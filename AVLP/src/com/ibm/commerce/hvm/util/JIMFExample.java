package com.ibm.commerce.hvm.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

//import com.google.common.collect.ImmutableList;
//import com.google.common.jimfs.Configuration;
//import com.google.common.jimfs.Jimfs;

public class JIMFExample {
	public static void main(final String[] args) throws IOException, InterruptedException {
		//runJIMFExample1();
		//runJIMFExample2();
		//runJIMFExample3();
		
	}

	/*
	private static void runJIMFExample1() throws IOException {
		System.out.println("JIMFExample 1: Creating, copying, reading files and directories");
		FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
		Path data = fs.getPath("/data");
		System.out.println("fs.getPath('/data') has: " + data);
		Files.createDirectory(data);

		Path hello = data.resolve("test.txt"); // /data/test.txt
		System.out.println("data.resolve('test.txt') has: " + hello);
		//Files.write(hello, ImmutableList.of("hello world"), StandardCharsets.UTF_8);
		Files.write(hello, "hello new world".getBytes());
		System.out.println("hello has: " + hello);
		System.out.println("read hello content has: " + Files.readAllLines(hello) + " " + Files.readAllBytes(hello).length);

		Path csv = data.resolve("data.csv"); // /data/data.csv
		Files.write(csv, ImmutableList.of("test1,test2\ntest3,test4"), StandardCharsets.UTF_8);

		InputStream istream = JIMFExample.class.getResourceAsStream("/book.xml");
		Path xml = data.resolve("book.xml"); // /data/book.xml
		Files.copy(istream, xml, StandardCopyOption.REPLACE_EXISTING);

		Files.list(data).forEach(file -> {
			try {
				System.out.println(String.format("%s (%db)", file, Files.readAllBytes(file).length));
				List<String> sl = Files.readAllLines(file);
				for(String s : sl){
					System.out.println(s);
				}
				System.err.println("Deleting " + file.toUri() + " " + file.toAbsolutePath());
				Files.deleteIfExists(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		System.out.println("list path data the second time");
		Files.list(data).forEach(file -> {
			try {
				System.out.println(String.format("Second list %s (%db)", file, Files.readAllBytes(file).length));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		try {
			System.out.println("Now delete path data");
			Files.deleteIfExists(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void runJIMFExample2() throws IOException {
		System.out.println("JIMFExample 2: Handling Symbolic Links");
		FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
		Path data = fs.getPath("/data");
		Files.createDirectory(data);

		Path hello = data.resolve("test.txt"); // /data/test.txt
		Files.write(hello, ImmutableList.of("hello world"), StandardCharsets.UTF_8);

		Path linkToHello = data.resolve("test.txt.link");
		Files.createSymbolicLink(linkToHello, hello);

		Files.list(data).forEach(file -> {
			try {
				System.out.println(String.format("%s (%db)", file, Files.readAllBytes(file).length));
				Files.deleteIfExists(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		try {
			Files.deleteIfExists(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void runJIMFExample3() throws IOException, InterruptedException {
		System.out.println("JIMFExample 3: Watching changes using a WatchService");
		FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
		Path data = fs.getPath("/data");
		Files.createDirectory(data);

		WatchService watcher = data.getFileSystem().newWatchService();
		Thread watcherThread = new Thread(() -> {
			WatchKey key;
			try {
				key = watcher.take();
				while (key != null) {
					for (WatchEvent<?> event : key.pollEvents()) {
						System.out.printf("event of type: %s received for file: %s\n", event.kind(), event.context());
					}
					key.reset();
					key = watcher.take();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, "CustomWatcher");
		watcherThread.start();
		data.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);

		Path hello = data.resolve("test.txt"); // /data/test.txt
		Files.write(hello, ImmutableList.of("hello world"), StandardCharsets.UTF_8);
		try {
			Files.deleteIfExists(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
}
