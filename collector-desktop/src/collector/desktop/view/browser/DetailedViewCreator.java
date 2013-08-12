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

package collector.desktop.view.browser;

import org.eclipse.swt.browser.Browser;

import collector.desktop.controller.GuiController;
import collector.desktop.controller.i18n.DictKeys;
import collector.desktop.controller.i18n.Translator;
import collector.desktop.model.album.AlbumItem;
import collector.desktop.model.album.AlbumItemStore;
import collector.desktop.view.ApplicationUI;
import collector.desktop.view.UIConstants;

public class DetailedViewCreator {	
	static void showDetailedAlbum(Browser browser) {
		// Exit if no album is selected
		if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
			return;
		}
		
		// Builders for efficient html creation
		StringBuilder albumItemTableRowHtml = new StringBuilder();
		StringBuilder htmlDataColumnContent = new StringBuilder();
		StringBuilder htmlPictureColumnContent = new StringBuilder();

		// Add all available album items to a html table
		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex())) {
			htmlDataColumnContent.delete(0, htmlDataColumnContent.length());
			htmlPictureColumnContent.delete(0, htmlPictureColumnContent.length());

			ItemCreator.addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml);
		}

		// If no album items have been found
		if (htmlDataColumnContent.length() == 0 && htmlPictureColumnContent.length() == 0) {
			albumItemTableRowHtml.delete(0, albumItemTableRowHtml.length());
			albumItemTableRowHtml.append(
	          "<tr><td><div>" + 
	            "<h3>" + 
	              Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND, GuiController.getGuiState().getSelectedAlbum()) + 
	            "</h3>" + 
	             Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND_EXPLANATION) + 
	          "</div></td></tr>"); 
		}

		// Create final page html
		String finalPageAsHtml = 
				"<!DOCTYPE HTML>" +
				"  <html>" +
				"    <head>" +
				"      <title>sammelbox.org</title>" +
				"      <meta " + UIConstants.META_PARAMS + ">" + 
				"      <link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
				"      <script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
				"    </head>" +
				"    <body style=\"background-color:#ffffff;font-family:" +  Utilities.getDefaultSystemFont() + ";margin:0\">" +
				"      <table width=95% style=\"margin:15px;\" id=\"albumItems\" border=0>" + albumItemTableRowHtml + "</table>" +
				"    </body>" +
				"  </html>";
		
		browser.setText(finalPageAsHtml);		
		Utilities.setLastPageAsHtml(finalPageAsHtml);		
	}
}
