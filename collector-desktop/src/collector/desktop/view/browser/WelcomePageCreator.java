package collector.desktop.view.browser;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.view.ApplicationUI;
import collector.desktop.view.internationalization.DictKeys;
import collector.desktop.view.internationalization.Translator;
import collector.desktop.view.managers.AlbumManager;
import collector.desktop.view.managers.WelcomePageManager;
import collector.desktop.view.various.Constants;

public class WelcomePageCreator {
	private static final int NUMBER_OF_FAVORITES_SHOWN = 5;

	static void loadWelcomePage() {
		ApplicationUI.getAlbumItemBrowser().setText(
				"<!DOCTYPE HTML>" +
				"<html>" +
				"  <head>" +
				"    <title>" + Translator.get(DictKeys.TITLE_MAIN_WINDOW) + "</title>" +
				"    <meta " + Constants.META_PARAMS + ">" + 
				"    <link rel=stylesheet href=\"" + Constants.STYLE_CSS + "\" />" +
				"    <script src=\"" + Constants.EFFECTS_JS + "\"></script>" +
				"  </head>" +
				"  <body>" +
				"    <font face=\"" + Utilities.getDefaultSystemFont() + "\">" +
				"	 <br>" +
				"	 <table>" +
				"      <tr>" +
				"	     <td align=\"center\">" +
				"          <img width=\"450\" src=\" " + FileSystemAccessWrapper.LOGO + " \">" +
				"        </td>" +
				"        <td width=\"430px\">" +
		        "          <div style=\"margin-left:30px; padding:10px; background-color:#" + Utilities.getBackgroundColorOfWidgetInHex() + "\">" +
						     generateAlbumInformation() +
						     generateFavorites() +
			    "          </div>" +
				"        </td>" +
				"      </tr>" +
				"	 </table>" +
				"    </font>" +
				"  </body>" +
				"</html>");
	}
	
	private static String generateAlbumInformation() {
		StringBuilder albumInformationBuilder = new StringBuilder("<h4>" + Translator.get(DictKeys.BROWSER_ALBUM_INFORMATION) + "</h4><ul>");

		if (AlbumManager.getAlbums().isEmpty()) {
			albumInformationBuilder.append("<li>" + 
										      Translator.get(DictKeys.BROWSER_NO_INFORMATION_AVAILABLE) + 
										   "</li>");
		} else {
			for (String album : AlbumManager.getAlbums()) {
				albumInformationBuilder.append("<li>" +
											   "  Album <b>" + album + "</b><br>" +
											   "  <font size=-1>" +
											   "  <i>" + 
													Translator.get(DictKeys.BROWSER_NUMBER_OF_ITEMS_AND_LAST_UPDATED, 
															WelcomePageManager.getNumberOfItemsInAlbum(album),
															WelcomePageManager.getLastModifiedDate(album)) +
											   "  </i>" +
											   "  </font>" +
											   "</li>");
			}
		}

		return albumInformationBuilder.append("</ul>").toString();
	}

	private static String generateFavorites() {
		StringBuilder favoriteBuilder = new StringBuilder("<h4>" + Translator.get(DictKeys.BROWSER_FAVORITE_ALBUMS_AND_VIEWS) + "</h4><ol>");

		int favCounter = 0;
		if (WelcomePageManager.getAlbumAndViewsSortedByClicks().keySet().isEmpty()) {
			favoriteBuilder.append("<li>" + 
									  Translator.get(DictKeys.BROWSER_NO_INFORMATION_AVAILABLE) + 
								   "</li>");
		} else {
			for (String albumOrViewName : WelcomePageManager.getAlbumAndViewsSortedByClicks().keySet()) {
				favoriteBuilder.append("<li>" +
										  albumOrViewName + 
										  "<font size=-1>" +
										    "<i>" + 
										      Translator.get(DictKeys.BROWSER_CLICKS_FOR_ALBUM, 
										  			WelcomePageManager.getAlbumAndViewsSortedByClicks().get(albumOrViewName)) + 
										    "</i>" +
										  "</font>" +
										"</li>");
	
				if (++favCounter == NUMBER_OF_FAVORITES_SHOWN) {
					break;
				}
			}
		}

		return favoriteBuilder.append("</ol>").toString();	
	}
}