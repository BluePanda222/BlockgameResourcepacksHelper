package com.bluepanda22.blockgameresourcepackshelper.util

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.UnzipParameters
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.File
import java.nio.file.Path

object ZipUtil {

	fun unzipFile(zipFilePath: Path, outputFilePath: Path) {
		unzipFile(zipFilePath.toFile(), outputFilePath.toFile())
	}

	fun unzipFile(zipFile: File, outputFile: File) {
		outputFile.mkdirs()

		val zip = ZipFile(zipFile)
		zip.extractAll(outputFile.path)

//		ZipFile(zipFile).use { zip ->
//			zip.entries().asSequence().forEach { entry ->
//				val entryFile = outputFile.resolve(entry.name)
//				if (entry.isDirectory) {
//					entryFile.mkdirs()
//				} else {
//					entryFile.parentFile.mkdirs()
//					zip.getInputStream(entry).use { input ->
//						entryFile.outputStream().use { output ->
//							input.copyTo(output)
//						}
//					}
//				}
//			}
//		}
	}


	fun zipDirectory(directoryPath: Path, outputZipFilePath: Path) {
		zipDirectory(directoryPath.toFile(), outputZipFilePath.toFile())
	}

	fun zipDirectory(directory: File, outputZipFile: File) {
		outputZipFile.parentFile.mkdirs()

		val zipFile = ZipFile(outputZipFile)
		val parameters = ZipParameters().apply {
			compressionMethod = CompressionMethod.DEFLATE
			compressionLevel = CompressionLevel.NORMAL
			isIncludeRootFolder = false
		}

		zipFile.addFolder(directory, parameters)

//		directory.listFiles()?.forEach { file: File ->
//			if (file.isDirectory) {
//				zipFile.addFolder(file, parameters)
//			} else {
//				zipFile.addFile(file, parameters)
//			}
//		}

//		directory.walkTopDown().forEach { file ->
//			if (file.isDirectory) {
//				zipFile.addFolder(file, parameters)
//			} else {
//				zipFile.addFile(file, parameters)
//			}
//		}

//		ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZipFile))).use { zipOutputStream ->
//			directory.walkTopDown().forEach { file ->
//				val entryName = directory.toPath().relativize(file.toPath()).toString()
//				if (file.isDirectory) {
//					val zipEntry = ZipEntry("$entryName/")
//					zipOutputStream.putNextEntry(zipEntry)
//				} else {
//					zipOutputStream.putNextEntry(ZipEntry(entryName))
//					FileInputStream(file).use { fileInputStream ->
//						fileInputStream.copyTo(zipOutputStream)
//					}
//				}
//				zipOutputStream.closeEntry()
//			}
//		}
	}


	fun embedFileIntoZip(
		targetZipFilePath: Path,
		fileToEmbedPath: Path,
		entryNameInZip: String,
		outputZipFilePath: Path
	) {
		embedFileIntoZip(targetZipFilePath.toFile(), fileToEmbedPath.toFile(), entryNameInZip, outputZipFilePath.toFile())
	}

	fun embedFileIntoZip(
		targetZipFile: File,
		fileToEmbed: File,
		entryNameInZip: String,
		outputZipFile: File
	) {
		val outputZip = ZipFile(outputZipFile)
		val targetZip = ZipFile(targetZipFile)
		val parameters = ZipParameters().apply {
			compressionMethod = CompressionMethod.DEFLATE
			compressionLevel = CompressionLevel.NORMAL
			fileNameInZip = entryNameInZip
//			rootFolderNameInZip = "backups"
		}

		if (fileToEmbed.isDirectory) {
			targetZip.addFolder(fileToEmbed, parameters)
		} else {
			targetZip.addFile(fileToEmbed, parameters)
		}

		targetZipFile.copyTo(outputZipFile)

//		ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZipFile))).use { zipOutputStream ->
//			// Copy entries from the target zip
//			ZipFile(targetZipFile).use { targetZip ->
//				targetZip.entries().asSequence().forEach { entry ->
//					zipOutputStream.putNextEntry(entry)
//					targetZip.getInputStream(entry).use { input ->
//						input.copyTo(zipOutputStream)
//
//						val entry = targetZip.getEntry(entryNameInZip)
//					}
//					zipOutputStream.closeEntry()
//				}
//			}
//
//			// Add the file to embed
//			zipOutputStream.putNextEntry(ZipEntry(entryNameInZip))
//			FileInputStream(fileToEmbed).use { input ->
//				input.copyTo(zipOutputStream)
//			}
//			zipOutputStream.closeEntry()
//		}
	}


	fun extractFileFromZip(
		targetZipFilePath: Path,
		entryNameInZip: String,
		outputFilePath: Path
	) {
		extractFileFromZip(targetZipFilePath.toFile(), entryNameInZip, outputFilePath.toFile())
	}

	fun extractFileFromZip(
		targetZipFile: File,
		entryNameInZip: String,
		outputFile: File
	) {
		outputFile.parentFile.mkdir()

		val zip = ZipFile(targetZipFile)
		zip.extractFile(entryNameInZip, outputFile.parentFile.path, entryNameInZip.substringAfterLast("/"))

//		ZipFile(targetZipFile).use { targetZip ->
//			val entry = targetZip.getEntry(entryNameInZip)
//			targetZip.getInputStream(entry).use { input ->
//				input.copyTo(outputFile.outputStream())
//			}
//		}
	}

}
