package org.davinci.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.davinci.server.internal.Links;
import org.davinci.server.internal.Links.Link;
import org.davinci.server.user.User;
import org.davinci.server.util.JSONWriter;

public class Resource {
	
	
	static ArrayList resourceFilter=new ArrayList();
	static {
		/* add some global filters */
		String[] filterNames = new String[]{};
		DirectoryFilter df = new DirectoryFilter(new String[]{IDavinciServerConstants.SETTINGS_DIRECTORY_NAME,
															  IDavinciServerConstants.DOWNLOAD_DIRECTORY_NAME,
															  IDavinciServerConstants.SVN_DIRECTORY_NAME});

		Resource.addFilter(df);
		Resource.addFilter(new WorkingCopyFilter());
	}
	
	public static void addFilter(IVResourceFilter filterName){
		resourceFilter.add(filterName);
	}
	
	public static boolean isHidden(IVResource file) {
		for(int i =0;i<Resource.resourceFilter.size();i++){
			IVResourceFilter filter = (IVResourceFilter)Resource.resourceFilter.get(i);
			if(filter.isHidden(file))
				return true;
		}
		
		return false;
	}
	
	public static String vRsourcesToJson(IVResource listFiles[], boolean fullPath){
		
		 JSONWriter jsonWriter = new  JSONWriter(true);
		 for (int j = 0; j < listFiles.length; j++) {
			if (Resource.isHidden(listFiles[j]))
					continue;
			
			String pathName = null;
			 if(fullPath)
				 pathName = listFiles[j].getPath();
			 else
				 pathName = listFiles[j].getName();
			 
			 jsonWriter.startObject().addField("name", pathName).addField("isDir", listFiles[j].isDirectory()).addField("isNew", listFiles[j].committed()).endObject();
		 }
		 return jsonWriter.getJSON();
	}
	
	public static String foundVRsourcesToJson(IVResource listFiles[]){
		
		 JSONWriter jsonWriter = new  JSONWriter(true);
		 for (int i = 0; i < listFiles.length; i++) {
			 ArrayList parents = new ArrayList();
		//	 parents.add(workspace);
			 if (Resource.isHidden(listFiles[i]))
					continue;
			 parents.addAll(Arrays.asList(listFiles[i].getParents()));
			
			 String name = listFiles[i].getPath();
			 jsonWriter.startObject().addField("file", name).addFieldName("parents").startArray();
			 for(int j=0;j<parents.size();j++){
				 if (Resource.isHidden((IVResource)parents.get(j)))
						continue;
			 	jsonWriter.startObject().addField("name",((IVResource)parents.get(j)).getName());
			 	jsonWriter.addFieldName("members").startArray();
			 	IVResource[] members = ((IVResource)parents.get(j)).listFiles();
			 	for(int k=0;k<members.length;k++){
			 		if (Resource.isHidden(members[k]))
						continue;
			 		jsonWriter.startObject().addField("isDir", members[k].isDirectory());
			 		jsonWriter.addField("name", members[k].getName());
			 		jsonWriter.endObject();
			 	}
				jsonWriter.endArray();
			 	jsonWriter.endObject();
			 
			 }
			 jsonWriter.endArray();
			 jsonWriter.endObject();
		 }
		 return jsonWriter.getJSON();
	}
	
	public static void directoryListJSON(IVResource dir, String path, User  user, JSONWriter jsonWriter){
		if (dir.exists()){
			IVResource[] listFiles = dir.listFiles();
			for (int j = 0; j < listFiles.length; j++) {
				IVResource member =listFiles[j];
				if (Resource.isHidden(member))
					continue;
				
				jsonWriter.startObject().addField("name", member.getName()).addField("isDir", member.isDirectory()).endObject();
			}
			List  links=user.getLinks().findLinks(path);
			for (Iterator iterator = links.iterator(); iterator.hasNext();) {
				Link link = (Link) iterator.next();
				jsonWriter.startObject().addField("name", link.name).addField("isDir",true).addField("link", link.location).endObject();
			}
			
		}
		/*
		if (path.equals(".") || path.length()==0 || jsonWriter.isEmpty())
		{
			ServerManager.getServerManger().getLibraryManager().listFiles(path,jsonWriter);
			
		}
		*/

	}
	
	public static String getVirtualPath(File file, User  user)
	{
		String path=file.getName();
		Links links = user.getLinks();
		IVResource rootDir=user.getWorkspace();
		File parent=file.getParentFile();
		while (!parent.equals(rootDir))
		{
			Link link =links.isLinkTarget(parent.getPath());
			if (link!=null)
			{
				path=link.path+"/"+path;
				break;
			}
			path=parent.getName()+"/"+path;
			parent=parent.getParentFile();

		}
		return path;
	}

}
