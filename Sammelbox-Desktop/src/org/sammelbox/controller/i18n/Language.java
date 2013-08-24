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

package org.sammelbox.controller.i18n;

public enum Language {
	UNKNOWN,
	ENGLISH,
	DEUTSCH;
	
	public static String getDictionaryBundle(Language language) {
		switch (language) {
		case ENGLISH:
			return "internationalization/dict_en";

		case DEUTSCH:
			return "internationalization/dict_de";
		
		default:
			return "internationalization/dict_en";
		}
	}
	
	public static String[] allLanguages() {
		String[] allLanguages = new String[values().length - 1];
		
		int i=0;
		for (Language language : values()) {
			if (language != UNKNOWN) {
				allLanguages[i++] = language.toString();
			}
		}
		
		return allLanguages;
	}
}
