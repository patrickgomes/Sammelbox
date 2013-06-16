package collector.desktop.tests.albumitems;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import collector.desktop.album.AlbumItem;
import collector.desktop.album.FieldType;
import collector.desktop.album.ItemField;
import collector.desktop.album.MetaItemField;
import collector.desktop.database.AlbumItemResultSet;
import collector.desktop.database.ConnectionManager;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.exceptions.FailedDatabaseWrapperOperationException;
import collector.desktop.filesystem.FileSystemAccessWrapper;

public class RemoveAlbumItemTests {
	/** Item field name to identify the item to be deleted.*/
	private final String dvdItemTitleColumnName = "DVD Title";
	/** Item field value to identify the item to be deleted.*/
	private final String dvdItemFieldValue = "dvd title 1";
	/** Name of the album where an item will be deleted */
	private final String dvdAlbumName = "DVD Album";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		resetFolderStructure();
		createDVDAlbum();
		fillDVDAlbum();
	}

	@After
	public void tearDown() throws Exception {
		resetFolderStructure();
	}

	@Test
	public void removeItemFromDVDAlbum() {
		String query = "SELECT id FROM '" + dvdAlbumName + "' WHERE ([" + dvdItemTitleColumnName + "] = '" +dvdItemFieldValue + "')";
		long albumItemId =-1;
		try {
			AlbumItemResultSet resultSet = DatabaseWrapper.executeSQLQuery(query);
			if (resultSet.moveToNext() == false || !resultSet.getFieldName(1).equals("id")) {
				fail("The id of the item to be deleted could not be retrieved");
			} 
			albumItemId  = resultSet.getFieldValue(1);

			DatabaseWrapper.deleteAlbumItem(dvdAlbumName, albumItemId);						
		}catch (FailedDatabaseWrapperOperationException e) {
			fail("Deletion of item with id: " + albumItemId + " failed!");
		}
		
		try { 
			AlbumItem item = DatabaseWrapper.fetchAlbumItem(dvdAlbumName, albumItemId);
			Assert.assertNull("Item should be null since it has been deleted!",item);
		} catch (FailedDatabaseWrapperOperationException e) {
			assertTrue(true);
		}
	}

	private void resetFolderStructure() {
		// Reset folder structure of the COLLECTOR HOME
		try {			
			ConnectionManager.closeConnection();

			FileSystemAccessWrapper.removeCollectorHome();

			Class.forName("org.sqlite.JDBC");

			FileSystemAccessWrapper.updateCollectorFileStructure();			

			ConnectionManager.openConnection();

			FileSystemAccessWrapper.updateAlbumFileStructure(ConnectionManager.getConnection());
		} 
		catch (Exception e) {
			e.printStackTrace();
			fail("Could not open database!");
		}
	}

	private void createDVDAlbum() {		
		// Create Album for insertion
		final String albumName = dvdAlbumName;

		MetaItemField DVDTitleField = new MetaItemField(dvdItemTitleColumnName, FieldType.Text, true);
		MetaItemField actorField = new MetaItemField("Actors", FieldType.Text, true);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(DVDTitleField);
		columns.add(actorField);
		try {
			DatabaseWrapper.createNewAlbum(albumName, columns, false);

		}catch (FailedDatabaseWrapperOperationException e) {
			fail("Creation of album "+ albumName + " failed");
		}
	}

	private void fillDVDAlbum() {
		final String albumName = "DVD Album";

		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField(dvdItemTitleColumnName, FieldType.Text, dvdItemFieldValue));
		fields.add( new ItemField("Actors", FieldType.Text, "actor 1"));

		item.setFields(fields);
		try {
			DatabaseWrapper.addNewAlbumItem(item, false, true);
		}catch (FailedDatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);

		fields = new ArrayList<ItemField>();
		fields.add( new ItemField(dvdItemTitleColumnName, FieldType.Text, "dvd title 2"));
		fields.add( new ItemField("Actors", FieldType.Text, "actor 2"));

		item.setFields(fields);
		try {
			DatabaseWrapper.addNewAlbumItem(item, false, true);			
		} catch (FailedDatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}
}
