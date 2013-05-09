package collector.desktop.tests.album;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import collector.desktop.database.AlbumItem;
import collector.desktop.database.AlbumItemResultSet;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.FieldType;
import collector.desktop.database.ItemField;
import collector.desktop.database.MetaItemField;
import collector.desktop.database.OptionType;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.tests.CollectorTestExecuter;

public class BackupRestoreTests {
	public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	
	private static void resetEverything() {
		System.out.println("Reset everything");
		try {			
			DatabaseWrapper.closeConnection();

			FileSystemAccessWrapper.removeCollectorHome();

			Class.forName("org.sqlite.JDBC");

			FileSystemAccessWrapper.updateCollectorFileStructure();			

			DatabaseWrapper.openConnection();

			FileSystemAccessWrapper.updateAlbumFileStructure(DatabaseWrapper.getConnection());
		} 
		catch (Exception e) {
			e.printStackTrace();
			fail("Could not open database!");
		}
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		resetEverything();
	}

	@Before
	public void setUp() {
		resetEverything();
	}

	@After
	public void tearDown() throws Exception {
		DatabaseWrapper.closeConnection();
	}

	private void createBookAlbum() {		
		final String albumName = "Books";

		MetaItemField titleField = new MetaItemField("Book Title", FieldType.Text, true);
		MetaItemField authorField = new MetaItemField("Author", FieldType.Text, true);
		MetaItemField purchaseField = new MetaItemField("Purchased", FieldType.Date, false);
		MetaItemField priceField = new MetaItemField("Price", FieldType.Number, false);
		MetaItemField lenttoField = new MetaItemField("Lent to", FieldType.Text, false);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(titleField);
		columns.add(authorField);
		columns.add(purchaseField);
		columns.add(priceField);
		columns.add(lenttoField);

		if (DatabaseWrapper.createNewAlbum(albumName, columns, true) == false) {
			fail("Creation of album "+ albumName + " failed");
		}
	}

	private void createDVDAlbum() {		
		// Create Album for insertion
		final String albumName = "DVD Album";

		MetaItemField DVDTitleField = new MetaItemField("DVD Title", FieldType.Text, true);
		MetaItemField actorField = new MetaItemField("Actors", FieldType.Text, true);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(DVDTitleField);
		columns.add(actorField);

		if (DatabaseWrapper.createNewAlbum(albumName, columns, false) == false) {
			fail("Creation of album "+ albumName + " failed");
		}
	}

	private void createMusicAlbum() {		
		// Create Album for insertion
		final String albumName = "Music Album";

		MetaItemField titleField = new MetaItemField("Title", FieldType.Text, true);
		MetaItemField artistField = new MetaItemField("Artist", FieldType.Text, true);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(titleField);
		columns.add(artistField);

		if (DatabaseWrapper.createNewAlbum(albumName, columns, true) == false) {
			fail("Creation of album "+ albumName + " failed");
		}
	}

	private void fillBookAlbum() {
		final String albumName = "Books";

		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book title 1"));
		fields.add( new ItemField("Author", FieldType.Text, "the author 1"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Number, 4.2d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name 1"));

		item.setFields(fields);

		if (DatabaseWrapper.addNewAlbumItem(item, false, true) == -1) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);

		fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book title 2"));
		fields.add( new ItemField("Author", FieldType.Text, "the author 2"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Number, 4.22d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name 2"));

		item.setFields(fields);

		if (DatabaseWrapper.addNewAlbumItem(item, false, true) == -1) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);

		fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book title 3"));
		fields.add( new ItemField("Author", FieldType.Text, "the author 3"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Number, 4.23d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name 3"));

		item.setFields(fields);

		if (DatabaseWrapper.addNewAlbumItem(item, false, true) == -1) {
			fail("Album Item could not be inserted into album");
		}
	}

	private void fillDVDAlbum() {
		final String albumName = "DVD Album";

		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("DVD Title", FieldType.Text, "dvd title 1"));
		fields.add( new ItemField("Actors", FieldType.Text, "actor 1"));

		item.setFields(fields);

		if (DatabaseWrapper.addNewAlbumItem(item, false, true) == -1) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);

		fields = new ArrayList<ItemField>();
		fields.add( new ItemField("DVD Title", FieldType.Text, "dvd title 2"));
		fields.add( new ItemField("Actors", FieldType.Text, "actor 2"));

		item.setFields(fields);

		if (DatabaseWrapper.addNewAlbumItem(item, false, true) == -1) {
			fail("Album Item could not be inserted into album");
		}
	}

	@Test
	public void testBackupOfSingleAlbum() {
		createBookAlbum();
		fillBookAlbum();

		// Check number of items in book album
		AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");

		assertTrue("Resultset should not be null", allAlbumItems != null);

		int counter = 0;
		while (allAlbumItems.moveToNext()) {
			counter++;
		}

		assertTrue("Resultset should contain 3 items", counter == 3);

		// Backup album
		DatabaseWrapper.backupToFile(TEMP_DIR + File.separatorChar + "testBackupRestoreOfSingleAlbum.cbk");
	}

	@Test
	public void testBackupOfMultipleAlbums() {
		createBookAlbum();
		createDVDAlbum();
		createMusicAlbum();
		fillBookAlbum();
		fillDVDAlbum();

		// Check number of items in book album
		AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");

		assertTrue("Resultset should not be null", allAlbumItems != null);

		int counter = 0;
		while (allAlbumItems.moveToNext()) {
			counter++;
		}

		assertTrue("Resultset should contain 3 items", counter == 3);

		// Check number of items in dvd album
		allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM 'DVD Album'");

		assertTrue("Resultset should not be null", allAlbumItems != null);

		counter = 0;
		while (allAlbumItems.moveToNext()) {
			counter++;
		}

		assertTrue("Resultset should contain 2 items", counter == 2);		

		// Check number of items in music album
		allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM 'Music Album'");

		assertTrue("Resultset should not be null", allAlbumItems != null);

		counter = 0;
		while (allAlbumItems.moveToNext()) {
			counter++;
		}

		assertTrue("Resultset should contain 0 items", counter == 0);	

		// Backup Albums
		DatabaseWrapper.backupToFile(TEMP_DIR + File.separatorChar + "testBackupRestoreOfMultipleAlbums.cbk");
	}

	@Test
	public void testRestoreOfSingleAlbum() {
		testBackupOfSingleAlbum();

		// Restore album
		DatabaseWrapper.restoreFromFile(TEMP_DIR + File.separatorChar + "testBackupRestoreOfSingleAlbum.cbk");

		// Check number of items in book album
		AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");

		assertTrue("Resultset should not be null", allAlbumItems != null);

		int counter = 0;
		while (allAlbumItems.moveToNext()) {
			counter++;
		}

		assertTrue("Resultset should contain 3 items", counter == 3);
	}

	@Test
	public void testRestoreOfMultipleAlbums() {
		testBackupOfMultipleAlbums();

		// Restore albums
		DatabaseWrapper.restoreFromFile(TEMP_DIR + File.separatorChar + "testBackupRestoreOfMultipleAlbums.cbk");

		// Check number of items in book album
		AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");

		assertTrue("Resultset should not be null", allAlbumItems != null);

		int counter = 0;
		while (allAlbumItems.moveToNext()) {
			counter++;
		}

		assertTrue("Resultset should contain 3 items", counter == 3);

		// Check number of items in dvd album
		allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM 'DVD Album'");

		assertTrue("Resultset should not be null", allAlbumItems != null);

		counter = 0;
		while (allAlbumItems.moveToNext()) {
			counter++;
		}

		assertTrue("Resultset should contain 2 items", counter == 2);		

		// Check number of items in music album
		allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM 'Music Album'");

		assertTrue("Resultset should not be null", allAlbumItems != null);

		counter = 0;
		while (allAlbumItems.moveToNext()) {
			counter++;
		}

		assertTrue("Resultset should contain 0 items", counter == 0);
	}

	@Test
	public void testRestoreOfTestDataAlbums() {
		assertTrue(new File(CollectorTestExecuter.PATH_TO_TEST_CBK).exists());

		// Restore albums
		DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

		// Check number of items in Books album
		AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");

		assertTrue("Resultset should not be null", allAlbumItems != null);

		int counter = 0;
		while (allAlbumItems.moveToNext()) {
			counter++;
		}

		assertTrue("Resultset should contain 10 items", counter == 10);

		// Check number of items in DVDs album
		allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM 'DVDs'");

		assertTrue("Resultset should not be null", allAlbumItems != null);

		counter = 0;
		while (allAlbumItems.moveToNext()) {
			counter++;
		}
		
		assertTrue("Resultset should contain 11 items", counter == 11);
	}
	
	@Test
	public void testRestoreAndModificiationOfTestDataAlbums() {
		testRestoreOfTestDataAlbums();
		
		final String albumName = "Books";

		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "added title"));
		fields.add( new ItemField("Author", FieldType.Text, "added author"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Lent to", FieldType.Text, "added person"));
		fields.add( new ItemField("Second Hand", FieldType.Option, OptionType.Yes));
		
		item.setFields(fields);

		if (DatabaseWrapper.addNewAlbumItem(item, false, true) == -1) {
			fail("Album Item could not be inserted into album");
		}
		
		DatabaseWrapper.backupToFile(TEMP_DIR + File.separatorChar + "testRestoreAndModificiationOfTestDataAlbums.cbk");
	}
	
	@Test
	public void testRestoreOfModificiationOfTestDataAlbums() {
		// Restore modified albums
		DatabaseWrapper.restoreFromFile(TEMP_DIR + File.separatorChar + "testRestoreAndModificiationOfTestDataAlbums.cbk");
		
		// Check number of items in Books album (must contain one more item than the original)
		AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");

		assertTrue("Resultset should not be null", allAlbumItems != null);

		int counter = 0;
		while (allAlbumItems.moveToNext()) {
			counter++;
		}
		
		assertTrue("Resultset should contain 11 items", counter == 11);
	}
}