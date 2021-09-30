/*
 * Copyright (c) 2008-2021, Massachusetts Institute of Technology (MIT)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.mit.ll.nics.tools.image_processing;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.file.FileSystemDirectory;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Uses the metadata-extractor project to process image files
 * <p>
 * groupId: com.drewnoakes<br/> artifactId: metadata-extractor https://github.com/drewnoakes/metadata-extractor
 * </p>
 */
public class ImageProcessor {

    /**
     * Default constructor
     */
    ImageProcessor() {
        super();
    }


    /**
     * Reads metadata from specified file
     *
     * @param imagePath Full path to image file to process
     * @return Metadata if successful, throws exception otherwise
     *
     * @throws ImageProcessingException
     * @throws IOException
     */
    public static Metadata readMetadata(String imagePath) throws ImageProcessingException, IOException {
        Metadata metadata = null;
        File in = null;

        try {
            in = new File(imagePath);
            metadata = ImageMetadataReader.readMetadata(in);

        } catch(ImageProcessingException e) {
            e.printStackTrace();
            throw new ImageProcessingException("Exception while processing " + imagePath + ": " + e.getMessage());
        } catch(IOException e) {
            e.printStackTrace();
            throw new IOException("Exception while processing " + imagePath + ": " + e.getMessage());
        }

        return metadata;
    }


    /**
     * Batch process files. Builds a key value map with the key being the image file path and the value being the
     * image's metadata.
     *
     * @param imagePaths String array of image file paths to process
     * @return a Map of filenames to Metadata if successful, an empty map otherwise. If a single file fails to retrieve
     * metadata, a null value is added to the map for that filename
     */
    public static Map<String, Metadata> readMetadata(String[] imagePaths) {

        if(imagePaths == null || imagePaths.length == 0) {
            return null;
        }

        Map<String, Metadata> metamap = new HashMap<String, Metadata>();

        Metadata curMetadata;
        for(String image : imagePaths) {
            curMetadata = null;
            try {
                curMetadata = readMetadata(image);
            } catch(IOException e) {
                e.printStackTrace();
            } catch(ImageProcessingException e) {
                e.printStackTrace();
            } catch(Exception e) {
                e.printStackTrace();
            }

            metamap.put(image, curMetadata);
        }

        return metamap;
    }


    /**
     * Helper method that calls getLocation(Metadata)
     *
     * @param filenamePath
     * @return
     */
    public static GeoLocation getLocation(String filenamePath) {

        try {
            return getLocation(readMetadata(filenamePath));
        } catch(ImageProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Checks for known Location in metadata
     *
     * @param metadata {@link Metadata} read from a file
     * @return A {@link GeoLocation} object if successful, null otherwise
     */
    public static GeoLocation getLocation(Metadata metadata) {

        if(metadata == null) {
            return null;
        }

        GeoLocation geoLocation = null;

        // TODO:nics-247 Handle case where multiple GPS directories exist?
        GpsDirectory geoDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);

        if(geoDir != null) {
            geoLocation = geoDir.getGeoLocation();
        }

        return geoLocation;
    }


    /**
     * Gets the File Last Modified Date from the Image metadata
     *
     * @param metadata
     * @return
     */
    public static Date getDate(Metadata metadata) {
        if(metadata == null) {
            return null;
        }

        Collection<FileSystemDirectory> fileMetadataDirs = metadata.getDirectoriesOfType(FileSystemDirectory.class);
        if(fileMetadataDirs == null) {
            return null;
        }

        Date date = null;
        FileSystemDirectory fileDir = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
        if(fileDir != null) {
            date = fileDir.getDate(FileSystemDirectory.TAG_FILE_MODIFIED_DATE);
        }

        // TODO:nics-247 other directory types have date, so to be thorough, could check more types

        return date;
    }


    /**
     * Helper method to printAllTags(Metadata)
     *
     * @param filenamePath The full path and filename to the image file to process
     */
    public static void printAllTags(String filenamePath) {
        try {
            printAllTags(readMetadata(filenamePath));
        } catch(ImageProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * Prints Tag.getDescription() of all Tags in Metadata
     *
     * @param metadata Metadata read from image file
     */
    public static void printAllTags(Metadata metadata) {
        if(metadata == null) {
            System.out.println("Metadata was null. Nothing to print.");
        }

        for(Directory dir : metadata.getDirectories()) {
            System.out.println("Directory: " + dir.getName());
            for(Tag tag : dir.getTags()) {
                System.out.println("\tTag: " + tag.getTagName() + ", Value: " + tag.getDescription());
            }
        }
    }

}
