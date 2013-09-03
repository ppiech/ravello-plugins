/*
 * 
 * Copyright (c) 2013 Ravello Systems Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 * @author Alex Nickolaevsky
 * */

package com.ravello.plugins.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.ravello.plugins.exceptions.ApplicationPropertiesException;

public class PluginIOService implements IOService {

	@Override
	public void writeToPropertiesFile(File file, Map<String, String> properties, PropertyKeyTrimmer trimmer)
			throws ApplicationPropertiesException {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file, "UTF-8");
			Set<String> keys = properties.keySet();
			for (String key : keys)
				writer.println(String.format("%s=%s", trimmer.trim(key), properties.get(key)));
		} catch (Exception e) {
			throw new ApplicationPropertiesException(e);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	@Override
	public File zipFile(File file, String zip) throws ApplicationPropertiesException {
		byte[] buffer = new byte[1024];
		ZipOutputStream zipOutputStream = null;
		FileInputStream fileInputStream = null;
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(zip);
			zipOutputStream = new ZipOutputStream(fileOutputStream);
			ZipEntry zipEntry = new ZipEntry(file.getName());
			zipOutputStream.putNextEntry(zipEntry);
			fileInputStream = new FileInputStream(file);

			int len;
			while ((len = fileInputStream.read(buffer)) > 0)
				zipOutputStream.write(buffer, 0, len);

		} catch (Exception e) {
			throw new ApplicationPropertiesException(e);
		} finally {
			try {
				fileInputStream.close();
				zipOutputStream.closeEntry();
				zipOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new File(zip);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void unzipFile(File _file, String extractTo) throws ApplicationPropertiesException {

		InputStream in = null;
		BufferedOutputStream out = null;
		ZipFile zipFile = null;

		try {
			zipFile = new ZipFile(_file);
			Enumeration<ZipEntry> enumeration = (Enumeration<ZipEntry>) zipFile.entries();
			while (enumeration.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) enumeration.nextElement();
				File extracted = new File(extractTo, entry.getName());
				if (entry.isDirectory() && !extracted.exists()) {
					extracted.mkdirs();
				} else {
					if (!extracted.getParentFile().exists()) {
						extracted.getParentFile().mkdirs();
					}
					in = zipFile.getInputStream(entry);
					out = new BufferedOutputStream(new FileOutputStream(extracted));
					byte[] buffer = new byte[2048];
					int read;
					while (-1 != (read = in.read(buffer))) {
						out.write(buffer, 0, read);
					}
				}
			}
		} catch (Exception e) {
			throw new ApplicationPropertiesException(e);
		} finally {
			try {
				in.close();
				out.close();
				zipFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, String> readProperties(File file) throws ApplicationPropertiesException {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
			Map<String, String> map = new HashMap(properties);
			return map;
		} catch (Exception e) {
			throw new ApplicationPropertiesException(e);
		}
	}

}
