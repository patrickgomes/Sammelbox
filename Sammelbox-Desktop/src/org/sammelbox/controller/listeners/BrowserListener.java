/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package org.sammelbox.controller.listeners;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.database.QueryBuilder;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.composites.StatusBarComposite;
import org.sammelbox.view.sidepanes.EmptySidepane;
import org.sammelbox.view.sidepanes.UpdateAlbumItemSidepane;
import org.sammelbox.view.various.ComponentFactory;
import org.sammelbox.view.various.PanelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserListener implements LocationListener, ProgressListener, MenuDetectListener {
	private final static Logger LOGGER = LoggerFactory.getLogger(BrowserListener.class);
	
	/** The parent composite to which this listener belongs to */
	private Composite parentComposite;

	/** Creates a new browser listener that can be attached to an SWT browser */
	public BrowserListener(Composite parentComposite) {
		this.parentComposite = parentComposite;
	}

	/** This method is required since under some circumstances, an question-mark is added to the end of the URL (event.location). If
	 * a question mark is present at the end, it will be deleted. Otherwise, the unmodified string is returned 
	 * @return a string without a question mark at the end*/
	private void removeQuestionMarkAtTheEndIfPresent(String string) {
		if (string.charAt(string.length() - 1) == '?') {
			string = string.substring(0, string.length() - 1);
		}
	}

	@Override
	public void changed(LocationEvent event) {}

	@Override
	/** If the current browser location is changing, it must be checked what the new location will be. Because the location may contain
	 * program specific commands such as "show:///updateComposite=2". In this case, the change is not performed, but instead an operation
	 * is executed. (E.g. opening a new composite) 
	 * @param event the location event used to identify the new location */
	public void changing(LocationEvent event) {		
		if (event.location.startsWith(UIConstants.SHOW_UPDATE_COMPOSITE)) {
			String id = event.location.substring(UIConstants.SHOW_UPDATE_COMPOSITE.length());
			removeQuestionMarkAtTheEndIfPresent(id);

			ApplicationUI.changeRightCompositeTo(PanelType.UpdateEntry,
					UpdateAlbumItemSidepane.build(parentComposite, ApplicationUI.getSelectedAlbum(), Long.parseLong(id)));
			BrowserFacade.jumpToAnchor(BrowserFacade.getAnchorForAlbumItemId(Integer.parseInt(id)));

			// Do not change the page
			event.doit = false;

		} else if (event.location.startsWith(UIConstants.SHOW_DELETE_COMPOSITE)) {
			String id = event.location.substring(UIConstants.SHOW_DELETE_COMPOSITE.length());
			removeQuestionMarkAtTheEndIfPresent(id);
		
			MessageBox messageBox = ComponentFactory.getMessageBox(parentComposite.getShell(),
					Translator.get(DictKeys.DIALOG_TITLE_DELETE_ALBUM_ITEM), 
					Translator.get(DictKeys.DIALOG_CONTENT_DELETE_ALBUM_ITEM), 
					SWT.ICON_WARNING | SWT.YES | SWT.NO);

			if (messageBox.open() == SWT.YES) {
				try {
					AlbumItem albumItemToBeDeleted = DatabaseOperations.getAlbumItem(ApplicationUI.getSelectedAlbum(), Long.parseLong(id));
					DatabaseOperations.deleteAlbumItem(albumItemToBeDeleted);
				} catch (NumberFormatException nfe) {
					LOGGER.error("Couldn't parse the following id: '" + id + "'", nfe);
				} catch (DatabaseWrapperOperationException ex) {
					LOGGER.error("A database error occured while deleting the album item #" + id + " from the album '" + 
										ApplicationUI.getSelectedAlbum() + "'", ex);
				}
				BrowserFacade.performBrowserQueryAndShow(QueryBuilder.createSelectStarQuery(ApplicationUI.getSelectedAlbum()));
			}

			// Do not change the page
			event.doit = false;
		} else if (event.location.startsWith(UIConstants.SHOW_BIG_PICTURE)) {
			String pathAndIdString = event.location.substring(UIConstants.SHOW_BIG_PICTURE.length());

			removeQuestionMarkAtTheEndIfPresent(pathAndIdString);

			String[] pathAndIdArray = pathAndIdString.split("\\?");

			BrowserFacade.showPicture(pathAndIdArray[0], Long.parseLong(pathAndIdArray[1].replace("imageId", "")));
			BrowserFacade.setFutureJumpAnchor(pathAndIdArray[1]);

			// Do not change the page
			event.doit = false;
		} else if (event.location.startsWith(UIConstants.SHOW_LAST_PAGE)) {
			BrowserFacade.goBackToLastPage();

			// Do not change the page
			event.doit = false;
		} else if (event.location.startsWith(UIConstants.SHOW_DETAILS)) {
			String id = event.location.substring(UIConstants.SHOW_DETAILS.length());
			removeQuestionMarkAtTheEndIfPresent(id);

			AlbumItem albumItem;
			try {
				albumItem = DatabaseOperations.getAlbumItem(ApplicationUI.getSelectedAlbum(), Long.parseLong(id));

				List<ItemField> itemFields = albumItem.getFields();
	
				StringBuilder sb = new StringBuilder();
				for (ItemField itemField : itemFields) {
					if (albumItem.getField(itemField.getName()).getType() != FieldType.ID && albumItem.getField(itemField.getName()).getType() != FieldType.UUID) {
						sb.append(albumItem.getField(itemField.getName()).getName() + ": " + albumItem.getField(itemField.getName()).getValue() + ", ");
					}
				}
				sb.append("...");
				
				StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(sb.toString());
			} catch (NumberFormatException nfe) {
				LOGGER.error("Couldn't parse the following id: '" + id + "'", nfe);
			} catch (DatabaseWrapperOperationException ex) {
				LOGGER.error("A database related error occured: \n Stacktrace: ", ex);
			}

			// Do not change the page
			event.doit = false;
		} else if (event.location.startsWith(UIConstants.SHOW_DETAILS_COMPOSITE)) {
			String id = event.location.substring(UIConstants.SHOW_DETAILS_COMPOSITE.length());
			removeQuestionMarkAtTheEndIfPresent(id);

			ApplicationUI.changeRightCompositeTo(PanelType.UpdateEntry,
					UpdateAlbumItemSidepane.build(parentComposite, ApplicationUI.getSelectedAlbum(), Long.parseLong(id)));

			BrowserFacade.jumpToAnchor(BrowserFacade.getAnchorForAlbumItemId(Long.parseLong(id)));

			// Do not change the page
			event.doit = false;
		} else if (event.location.startsWith("file:///")) {
			if (event.location.contains(".collector/app-data/loading.html")) {
				event.doit = true;
			} else {
				event.doit = false;
			}
		} else if (event.location.equals(UIConstants.ADD_ADDITIONAL_ALBUM_ITEMS)) {
			BrowserFacade.addAdditionalAlbumItems();

			// Do not change the page
			event.doit = false;
		} else if (event.location.equals(UIConstants.SHOW_DETAILS_VIEW_OF_ALBUM)) {
			BrowserFacade.performBrowserQueryAndShow(QueryBuilder.createSelectStarQuery(ApplicationUI.getSelectedAlbum()));
			
			ApplicationUI.changeRightCompositeTo(PanelType.Empty, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
			// Do not change the page
			event.doit = false;
		}
	}

	@Override
	public void changed(ProgressEvent event) {
	}

	@Override
	/** As soon as a page is completely loaded, it is possible to jump to a previously defined anchor */
	public void completed(ProgressEvent event) {
		BrowserFacade.jumpToAnchor(BrowserFacade.getFutureJumpAnchor());
	}

	@Override
	/** Don't show the usual browser menu */
	public void menuDetected(MenuDetectEvent e) {
		e.doit = false;
	}
}