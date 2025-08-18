package com.bluepanda22.blockgameresourcepackshelper.util

import java.io.File
import java.io.InputStream
import java.nio.file.Path

object FileUtil {

	fun writeToFile(string: String, directory: Path, fileName: String) {
		return writeToFile(string, directory.toFile(), fileName)
	}

	fun writeToFile(string: String, directory: File, fileName: String) {
		if (!directory.exists()) {
			directory.mkdir()
		}

		val file = File(directory, fileName)
		file.writeText(string)
	}


	fun readFromFile(directory: Path, fileName: String): String {
		return readFromFile(directory.toFile(), fileName)
	}

	fun readFromFile(directory: File, fileName: String): String {
		val configFile = File(directory, fileName)
		return configFile.readText()
	}


	fun checkIfFileExists(directory: Path, fileName: String): Boolean {
		return checkIfFileExists(directory.toFile(), fileName)
	}

	fun checkIfFileExists(directory: File, fileName: String): Boolean {
		val configFile = File(directory, fileName)
		return configFile.exists()
	}


	fun getFileFromResourceAsStream(fileName: String): InputStream {
		val classLoader = javaClass.classLoader
		val inputStream = classLoader.getResourceAsStream(fileName)

		requireNotNull(inputStream) { "file not found! $fileName" }
		return inputStream
	}

}
