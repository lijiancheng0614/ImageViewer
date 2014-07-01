package imageViewer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import com.drew.metadata.exif.ExifIFD0Directory;

public class MetaDataReader {
	public static Collection<Tag> getTags(String fileName) {
		File jpegFile = new File(fileName);
		Metadata metadata;
		try {
			metadata = JpegMetadataReader.readMetadata(jpegFile);
			Directory exif = metadata.getDirectory(ExifIFD0Directory.class);
			if (exif != null) {
				Collection<Tag> tags = exif.getTags();
				return tags;
			}
		} catch (JpegProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}