/*
 *  Copyright 2015 LG CNS.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *
 */
package scouter.client;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

import scouter.client.group.view.GroupNavigationView;
import scouter.client.preferences.PManager;
import scouter.client.preferences.PreferenceConstants;
import scouter.client.server.Server;
import scouter.client.server.ServerManager;
import scouter.client.views.AlertView;
import scouter.client.views.EQView;
import scouter.client.views.ObjectActiveServiceListView;
import scouter.client.views.ObjectDailyListView;
import scouter.client.views.ObjectNavigationView;
import scouter.client.views.ObjectThreadDetailView;
import scouter.client.views.WorkspaceExplorer;
import scouter.client.xlog.views.XLogDependencyView;
import scouter.client.xlog.views.XLogProfileView;
import scouter.client.xlog.views.XLogRealTimeView;
import scouter.client.xlog.views.XLogSelectionView;
import scouter.client.xlog.views.XLogZoomTimeView;

public class PerspectiveService implements IPerspectiveFactory  {
	
	public static final String ID = PerspectiveService.class.getName();
	
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		String objType = PManager.getInstance().getString(PreferenceConstants.P_PERS_WAS_SERV_DEFAULT_WAS);
		
		Server server = ServerManager.getInstance().getDefaultServer();
		int serverId = 0;
		if(server != null){
			serverId = server.getId();
		}
		
		IFolderLayout agentLayout = layout.createFolder(IConstants.LAYOUT_WASSERVICE_OBJECT_NAVIGATION, IPageLayout.LEFT, 0.20f, editorArea);
		agentLayout.addPlaceholder(ObjectNavigationView.ID + ":*");
		agentLayout.addPlaceholder(ObjectDailyListView.ID + ":*");
		agentLayout.addPlaceholder(WorkspaceExplorer.ID);
		agentLayout.addPlaceholder(GroupNavigationView.ID);
		agentLayout.addView(ObjectNavigationView.ID);
		layout.getViewLayout(ObjectNavigationView.ID).setCloseable(false); 
		 
		IFolderLayout eqLayout = layout.createFolder(IConstants.LAYOUT_WASSERVICE_ALERT, IPageLayout.BOTTOM, 0.5f, IConstants.LAYOUT_WASSERVICE_OBJECT_NAVIGATION);
		eqLayout.addPlaceholder(AlertView.ID);
		eqLayout.addPlaceholder(EQView.ID + ":*");
		eqLayout.addView(EQView.ID + ":" + serverId +"&"+ objType); // 1
		
		IFolderLayout xlogTopLayout = layout.createFolder(IConstants.LAYOUT_WASSERVICE_CENTER_TOP, IPageLayout.LEFT, 1f, editorArea);
		xlogTopLayout.addPlaceholder(XLogProfileView.ID + ":*");
		xlogTopLayout.addPlaceholder(ObjectThreadDetailView.ID + ":*");
		xlogTopLayout.addView(XLogRealTimeView.ID + ":" + serverId + "&" + objType);
		
		IPlaceholderFolderLayout xlogLayout = layout.createPlaceholderFolder(IConstants.LAYOUT_WASSERVICE_CENTER, IPageLayout.BOTTOM, 0.5f, IConstants.LAYOUT_WASSERVICE_CENTER_TOP);
		xlogLayout.addPlaceholder(XLogSelectionView.ID + ":*");
		xlogLayout.addPlaceholder(ObjectActiveServiceListView.ID + ":*");
		xlogLayout.addPlaceholder(XLogDependencyView.ID + ":*");
		xlogLayout.addPlaceholder(XLogZoomTimeView.ID + ":*");

		layout.addPerspectiveShortcut(getId());
	}
	
	public static String getId() {
		return ID;
	}
}
