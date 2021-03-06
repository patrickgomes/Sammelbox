/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
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

package org.sammelbox.controller.managers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jdbcdslog.ConnectionLoggingProxy;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConnectionManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
	private static final String SQLITE_CONNECTION_STRING = "jdbc:sqlite:";
	private static Connection connection = null;

	private ConnectionManager() {
		// not needed
	}
	
	/**
	 * Opens the default connection for the FileSystemAccessWrapper.DATABASE database. Only opens a new connection if none is currently open.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static synchronized void openConnection() throws DatabaseWrapperOperationException {
		// Catch the internal SQL exception to give a definite state on the database connection using the collector exceptions
		// This hides all internal SQL exceptions
		try {
			if (ConnectionManager.connection == null || connection.isClosed()) {
				ConnectionManager.connection = DriverManager.getConnection(ConnectionManager.SQLITE_CONNECTION_STRING + 
						FileSystemLocations.getDatabaseFile());
				ConnectionManager.connection = ConnectionLoggingProxy.wrap(connection);
				ConnectionManager.enableForeignKeySupportForCurrentSession();
				
				// The AutoCommit state makes little difference here since all relevant public methods roll back on
				// failures anyway and we have only a single connection so concurrency is not relevant either.		
				ConnectionManager.connection.setAutoCommit(true);

				LOGGER.info("Autocommit is on {}", connection.getAutoCommit());				
			}
			
			// Create the album master table if it does not exist 
			DatabaseOperations.createAlbumMasterTableIfItDoesNotExist();

			// Run a fetch  to check if the database connection is up and running
			if (!ConnectionManager.isConnectionReady()) {
				throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE);
			}
		} catch (SQLException sqlEx) {			
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, sqlEx);
		}
	}

	/**
	 * Tries to close the database connection. If the connection is closed or null calling this method has no effect.
	 * @throws DatabaseWrapperOperationException
	 */
	public static synchronized void closeConnection() throws DatabaseWrapperOperationException {
		try {
			if (ConnectionManager.connection != null && !ConnectionManager.connection.isClosed()) {
				ConnectionManager.connection.close();
			}
		} catch (SQLException sqlEx) {
			LOGGER.error("Unable to close the database connection");
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, sqlEx);
		}
	}

	/**
	 * Test if the connection is open and ready to be used. 
	 */
	public static synchronized boolean isConnectionReady() {
		try {
			// Querying all albums should be successful on all working databases, independently of how many albums are stored.
			// If not, (e.g. due to connection problems) or missing albums, indicate the failure
			if (ConnectionManager.connection == null || ConnectionManager.connection.isClosed() || DatabaseOperations.getListOfAllAlbums() == null) {			
				return false;
			}
		} catch (SQLException | DatabaseWrapperOperationException ex) {
			LOGGER.error("Unable to test the database connection", ex);
			return false;			
		}
		
		return true;
	}

	/**
	 * This method can be used when the database connection cannot be opened (e.g. corrupt database file).
	 * I saves the collector home for manual inspection, then clears the whole  collector home including the database
	 * and opens a connection to a blank database.
	 * If the connection is unexpectedly in a usable state it, this is a no-operation.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static synchronized void openCleanConnection() throws DatabaseWrapperOperationException {
		if (isConnectionReady()) {
			return;
		}

		closeConnection();			

		String corruptSnapshotFileName = DatabaseIntegrityManager.CORRUPT_DATABASE_SNAPSHOT_PREFIX + System.currentTimeMillis();
		File corruptTemporarySnapshotFile = new File(FileSystemLocations.USER_HOME + File.separator + corruptSnapshotFileName);
		corruptTemporarySnapshotFile.deleteOnExit();
		// Copy file to temporary location
		try {
			FileSystemAccessWrapper.copyFile(new File(FileSystemLocations.getDatabaseFile()), corruptTemporarySnapshotFile);
		} catch (IOException ioe) {
			LOGGER.error("Copying the corrupt database file to a temporary location failed" , ioe);
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, ioe);
		}

		// Clean home directory
		FileSystemAccessWrapper.removeHomeDirectory();

		// Copy the corrupt snapshot from the temporary location into the app data folder 
		String corruptSnapshotFilePath = FileSystemLocations.getActiveHomeDir() + File.separator + corruptSnapshotFileName;
		File corruptSnapshotFile = new File(corruptSnapshotFilePath);			
		try {
			FileSystemAccessWrapper.copyFile(corruptTemporarySnapshotFile, corruptSnapshotFile);
		} catch (IOException ioe) {
			LOGGER.error("Copying the corrupt database file from the temporary location back to the clean home directory failed. Manual cleanup may be required", ioe);
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, ioe);
		}

		// Try to open a regular connection to the newly setup home directory
		openConnection();

		if (!FileSystemAccessWrapper.updateSammelboxFileStructure()) {
			LOGGER.error("Updating the structure of the home directory failed. Manual cleanup may be required");
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE);
		}
	}

	/**
	 * Gets the connection.
	 * @return A valid connection or null if not properly initialized.
	 */
	public static synchronized Connection getConnection() {
		return connection;
	}

	static void enableForeignKeySupportForCurrentSession() throws DatabaseWrapperOperationException {

		try (PreparedStatement preparedStatement = connection.prepareStatement("PRAGMA foreign_keys = ON");) {			
			preparedStatement.executeUpdate();
		} catch (SQLException sqlEx) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, sqlEx);
		} 
	}
}
