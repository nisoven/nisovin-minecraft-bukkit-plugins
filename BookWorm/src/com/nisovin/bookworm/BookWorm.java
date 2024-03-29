package com.nisovin.bookworm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.Configuration;

import com.nisovin.bookworm.Metrics.Graph.Type;
import com.nisovin.bookworm.event.*;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class BookWorm extends JavaPlugin {

	protected static String STACK_BY_DATA_FN = "a";
	
	protected static boolean ENABLE_STAT_COLLECTION = true;
	
	protected static ChatColor TEXT_COLOR = ChatColor.GREEN;
	protected static ChatColor TEXT_COLOR_2 = ChatColor.WHITE;
	protected static boolean ENABLE_COLORS = true;
	protected static String CHARSET = "UTF-8";
	
	protected static int LINE_LENGTH = 55;
	protected static int PAGE_LENGTH = 6;
	protected static String INDENT = "    ";
	protected static String NEW_PARA = "::";
	
	protected static boolean SHOW_TITLE_ON_HELD_CHANGE = true;
	protected static boolean REQUIRE_BOOK_TO_COPY = false;
	protected static boolean MAKE_REAL_COPY = false;
	protected static boolean CHECK_WORLDGUARD = true;
	protected static boolean USE_FULL_FILENAMES = true;
	protected static boolean AUTO_CHAT_MODE = true;
	protected static boolean DROP_BOOKSHELF = false;
	protected static boolean KEEP_ALL_BOOKS_LOADED = false;
	protected static boolean USE_SPOUT_FEATURES = true;
	protected static boolean SPOUT_ENABLED = false;
	protected static boolean USE_DENY_PERMS = false;
	protected static boolean SAVE_BOOKCASES_IN_WORLD_FOLDER = false;
	
	protected static int CLEAN_INTERVAL = 600;
	protected static int REMOVE_DELAY = 300;
		
	protected static String S_MUST_HOLD_BOOK = "You must be holding a single book to write.";
	protected static String S_USAGE_START = "Use /%c <title> to start your book.";
	protected static String S_USAGE_WRITE = 
			"Use /%c <text> to add text to your book.\n" +
			"You can use a double-colon :: to create a new paragraph.\n" +
			"Left click on a bookcase to place your book.\n" +
			"Type /%c -help to see special commands.";
	protected static String S_USAGE_READ = "Right click to read.";
	protected static String S_NEW_BOOK_CREATED = "New book created: %t";
	protected static String S_CANNOT_DESTROY = "You can't destroy someone else's bookshelf!";
	protected static String S_NO_BOOK = "";
	
	protected static String S_COMM_HELP = "help";
	protected static String S_COMM_GET = "get";
	protected static String S_COMM_ID = "id";
	protected static String S_COMM_READ = "read";
	protected static String S_COMM_TITLE = "title";
	protected static String S_COMM_AUTHOR = "author";
	protected static String S_COMM_UNDO = "undo";
	protected static String S_COMM_ERASE = "erase";
	protected static String S_COMM_REPLACE = "replace";
	protected static String S_COMM_ERASEALL = "eraseall";	
	protected static String S_COMM_CHATMODE = "chat";

	protected static String S_COMM_HELP_TEXT = "Special commands:\n" +
			"   /%c -chat -- toggle chat write mode\n" +
			"   /%c -read <page> -- read the specified page\n" +
			"   /%c -undo -- undo the last write action\n" +
			"   /%c -erase <text> -- erase the given text\n" +
			"   /%c -replace <old text> -> <new text> -- replace text\n" +
			"   /%c -title <new title> -- change the book's title\n" +
			"   /%c -eraseall -- erase the entire book";	
	protected static String S_COMM_GET_DONE = "Got book: ";
	protected static String S_COMM_GET_FAIL = "Unable to get book.";
	protected static String S_COMM_ID_DONE = "Book id: ";
	protected static String S_COMM_ID_FAIL = "You are not holding a book.";
	protected static String S_COMM_TITLE_DONE = "Title changed: ";
	protected static String S_COMM_AUTHOR_DONE = "Author changed: ";
	protected static String S_COMM_UNDO_DONE = "Undo successful.";
	protected static String S_COMM_UNDO_FAIL = "Unable to undo.";
	protected static String S_COMM_ERASE_DONE = "Text erased.";
	protected static String S_COMM_REPLACE_DONE = "Text replaced.";
	protected static String S_COMM_REPLACE_FAIL = "Text not found.";
	protected static String S_COMM_ERASEALL_DONE = "Book contents erased.";
	protected static String S_COMM_CHATMODE_ON = "Chat write mode enabled.";
	protected static String S_COMM_CHATMODE_OFF = "Chat write mode disabled.";
	protected static String S_COMM_INVALID = "Invalid command.";
	
	protected static String S_WRITE_DONE = "Wrote line: %t";
	protected static String S_WRITE_FAIL = "You cannot write in a book that is not yours.";
	
	protected static String S_READ_DIVIDER = "--------------------------------------------------";
	protected static String S_READ_BOOK = "Book";
	protected static String S_READ_BY = "by";
	protected static String S_READ_PAGE = "page";
	protected static String S_COPIED_BOOK = "Copied book:";
	protected static String S_REMOVED_BOOK = "Removed book:";
	protected static String S_PLACED_BOOK = "Book placed in bookshelf:";
	protected static String S_PLACED_BOOK_FAIL = "You cannot put a book here.";
	protected static String S_NO_PERMISSION = "You do not have permission to do that.";
		
	protected static BookWorm plugin;
	protected PermissionManager perms;
	
	protected HashMap<Short,Book> books;
	protected HashMap<String,Short> bookshelves;
	protected HashMap<String,Bookmark> bookmarks;
	protected Set<String> chatModed;
	protected BookUnloader unloader;
	protected WorldGuardPlugin worldGuard;
	protected ArrayList<Short> extraBookIds;
	
	protected static int metricBookCount = 0;
	protected static int metricReads = 0;
	protected static int metricWrites = 0;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		// make sure save folder exists
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		// setup storage
		perms = new PermissionManager();
		books = new HashMap<Short,Book>();
		bookshelves = new HashMap<String,Short>();
		bookmarks = new HashMap<String,Bookmark>();
		chatModed = Collections.synchronizedSet(new HashSet<String>());
		
		// load up stuff
		loadConfig();
		loadBookshelves();
		loadExtraBookIds();
		
		// register command
		getCommand("bookworm").setExecutor(new BookWormCommandExecutor(this));
		
		// register listeners
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new BookWormPlayerListener(this), this);
		pm.registerEvents(new BookWormBlockListener(this), this);
		pm.registerEvents(new BookWormWorldListener(this), this);
		
		// check for spout
		if (USE_SPOUT_FEATURES) {
			Plugin spout = getServer().getPluginManager().getPlugin("Spout");
			if (spout != null) {
				SPOUT_ENABLED = true;
				pm.registerEvents(new BookWormSpoutScreenListener(this), this);
				getServer().getLogger().info("BookWorm 'Spout' support enabled.");
			} else {
				SPOUT_ENABLED = false;
			}
		}
		
		// start memory cleaner
		if (CLEAN_INTERVAL > 0 && !KEEP_ALL_BOOKS_LOADED) {
			unloader = new BookUnloader(this);
		}
		
		// load all books
		if (KEEP_ALL_BOOKS_LOADED) {
			short maxId = getCurrentBookId();
			if (maxId > 0) {
				for (short i = 0; i < maxId; i++) {
					getBookById(i);
				}
			}
		}
		
		// initialize world guard
		if (CHECK_WORLDGUARD) {
			Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
			if (plugin != null) {
				worldGuard = (WorldGuardPlugin)plugin;
			}
		}
		
		// setup metrics
		if (ENABLE_STAT_COLLECTION) {
			setupMetrics();
		}
		
		getServer().getLogger().info("BookWorm v" + this.getDescription().getVersion() + " loaded!");
		
	}
	
	private void setupMetrics() {
		try {
			Metrics metrics = new Metrics();
			
			metricBookCount = getDataFolder().list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".txt") && !name.equals("bookid.txt") && !name.equals("bookshelves.txt") && !name.equals("extrabookids.txt")) {
						return true;
					} else {
						return false;
					}
				}				
			}).length;
			
			Metrics.Graph bookCountGraph = metrics.createGraph(this, Type.Line, "Book Count");			
			bookCountGraph.addPlotter(new Metrics.Plotter("Book Count") {
				public int getValue() {
					return metricBookCount;
				}
			});
			
			Metrics.Graph actionsGraph = metrics.createGraph(this, Type.Line, "Actions");
			actionsGraph.addPlotter(new Metrics.Plotter("Pages Read") {
				public int getValue() {
					int reads = metricReads;
					metricReads = 0;
					return reads;
				}
			});			
			actionsGraph.addPlotter(new Metrics.Plotter("Lines Written") {
				public int getValue() {
					int writes = metricWrites;
					metricWrites = 0;
					return writes;
				}
			});
			
			metrics.beginMeasuringPlugin(this);
		} catch (IOException e) {
		}
	}
	
	/**
	 * Gets the book a player is holding, if the player is holding a BookWorm book.
	 * @param player the player
	 * @return the book the player is holding, or null if the player is not holding a BookWorm book
	 */
	public static Book getBook(Player player) {
		return getBook(player.getItemInHand());
	}
	
	/**
	 * Gets the BookWorm book from an ItemStack.
	 * @param item the item
	 * @return the book, or null if it is not a BookWorm book
	 */
	public static Book getBook(ItemStack item) {
		if (item == null || item.getType() != Material.BOOK || item.getDurability() == 0) {
			return null;
		} else {
			return plugin.getBookById(item.getDurability());
		}
	}
	
	/**
	 * Gets the book contained in a bookshelf, if there is one.
	 * @param block the bookshelf block
	 * @return the book, or null if there is no book there
	 */
	public static Book getBook(Block block) {
		return getBook(block.getLocation());
	}
	
	/**
	 * Gets the book contained in a bookshelf, if there is one.
	 * @param location the location of the bookshelf block
	 * @return the book, or null if there is no book there
	 */
	public static Book getBook(Location location) {
		String locStr = location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
		Short id = plugin.bookshelves.get(locStr);
		if (id == null) {
			return null;
		} else {
			return plugin.getBookById(id);
		}
	}
	
	/**
	 * Gets the book with the specified id number.
	 * @param id the book id
	 * @return the book, or null if no book with that id exists
	 */
	public static Book getBook(short id) {
		return plugin.getBookById(id);
	}
	
	/**
	 * Creates a new BookWorm book.
	 * @param author the new book's author
	 * @param title the new book's title
	 * @return the newly created book, or null if something went wrong
	 */
	public static Book createBook(String author, String title) {
		short bookId = plugin.getNextBookId();
		if (bookId == -1) {
			return null;
		}
		
		// setup book
		Book book = new Book(bookId, title, author);
		plugin.books.put(bookId, book);
		BookWorm.metricBookCount++;
		
		return book;
	}
	
	protected Book getBookById(short id) {
		Book book = books.get(id);
		if (book == null) {
			book = new Book(id);
			if (book.load()) {
				books.put(id, book);
			} else {
				book = null;
			}
		}
		return book;
	}
	
	protected Book copyBook(Book book) {
		short id = getNextBookId();
		if (id == -1) {
			return null;
		}
		Book copy = new Book(id, book);
		books.put(id, copy);
		return copy;
	}
	
	protected static String colorize(String s) {
		return s.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
	}
	
	protected void callEvent(BookEvent event) {
		getServer().getPluginManager().callEvent(event);
	}
	
	/**
	 * Gets the BookWorm permission handler
	 * @return the permission handler
	 */
	public static PermissionManager getPermissions() {
		return plugin.perms;
	}
	
	protected void loadBookshelves() {
		try {
			Scanner scanner = new Scanner(new File(getDataFolder(), "bookshelves.txt"));
			while (scanner.hasNext()) {
				String[] line = scanner.nextLine().split(":");
				bookshelves.put(line[0], Short.parseShort(line[1]));
			}
			scanner.close();
		} catch (FileNotFoundException e) {
		}
	}
	
	protected void loadExtraBookIds() {
		extraBookIds = new ArrayList<Short>();
		try {
			Scanner scanner = new Scanner(new File(getDataFolder(), "extrabookids.txt"));
			while (scanner.hasNext()) {
				extraBookIds.add(Short.parseShort(scanner.nextLine()));
			}
			scanner.close();
		} catch (FileNotFoundException e) {
		}
	}
	
	protected void loadConfig() {
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			saveDefaultConfig();
		}
		Configuration config = getConfig();
		
		STACK_BY_DATA_FN = config.getString("general.stack-by-data-fn", STACK_BY_DATA_FN);
		
		ENABLE_STAT_COLLECTION = config.getBoolean("general.enable-usage-stat-collection", ENABLE_STAT_COLLECTION);
		
		TEXT_COLOR = ChatColor.getByChar(config.getString("general.text-color", TEXT_COLOR.getChar()+""));
		TEXT_COLOR_2 = ChatColor.getByChar(config.getString("general.text-color-2", TEXT_COLOR_2.getChar()+""));
		ENABLE_COLORS = config.getBoolean("general.enable-color-codes", ENABLE_COLORS);
		CHARSET = config.getString("general.charset", CHARSET);
		
		SHOW_TITLE_ON_HELD_CHANGE = config.getBoolean("general.show-title-on-held-change", SHOW_TITLE_ON_HELD_CHANGE);
		REQUIRE_BOOK_TO_COPY = config.getBoolean("general.require-book-to-copy", REQUIRE_BOOK_TO_COPY);
		MAKE_REAL_COPY = config.getBoolean("general.make-real-copy", MAKE_REAL_COPY);
		AUTO_CHAT_MODE = config.getBoolean("general.auto-chat-mode", AUTO_CHAT_MODE);
		DROP_BOOKSHELF = config.getBoolean("general.drop-bookshelf-on-break", DROP_BOOKSHELF);
		KEEP_ALL_BOOKS_LOADED = config.getBoolean("general.keep-all-books-loaded", KEEP_ALL_BOOKS_LOADED);
		USE_SPOUT_FEATURES = config.getBoolean("general.use-spout-features", USE_SPOUT_FEATURES);
		USE_DENY_PERMS = config.getBoolean("general.use-deny-perms", USE_DENY_PERMS);
		SAVE_BOOKCASES_IN_WORLD_FOLDER = config.getBoolean("general.save-bookcases-in-world-folders", SAVE_BOOKCASES_IN_WORLD_FOLDER);

		CHECK_WORLDGUARD = config.getBoolean("general.check-worldguard", CHECK_WORLDGUARD);
		USE_FULL_FILENAMES = config.getBoolean("general.use-full-filenames", USE_FULL_FILENAMES);
		CLEAN_INTERVAL = config.getInt("general.clean-interval", CLEAN_INTERVAL);
		REMOVE_DELAY = config.getInt("general.remove-delay", REMOVE_DELAY);
		
		LINE_LENGTH = config.getInt("formatting.line-length", LINE_LENGTH);
		PAGE_LENGTH = config.getInt("formatting.page-length", PAGE_LENGTH);
		int indent = config.getInt("formatting.indent-size", INDENT.length());
		INDENT = "";
		for (int i = 0; i < indent; i++) {
			INDENT += " ";
		}
		
		S_MUST_HOLD_BOOK = config.getString("strings.must-hold-book", S_MUST_HOLD_BOOK);
		S_USAGE_START = config.getString("strings.usage-start", S_USAGE_START);
		S_USAGE_WRITE = config.getString("strings.usage-write", S_USAGE_WRITE);
		S_USAGE_READ = config.getString("strings.usage-read", S_USAGE_READ);
		S_NEW_BOOK_CREATED = config.getString("strings.new-book-created", S_NEW_BOOK_CREATED);
		S_CANNOT_DESTROY = config.getString("strings.cannot-destroy", S_CANNOT_DESTROY);
		S_NO_BOOK = config.getString("strings.no-book-in-bookshelf", S_NO_BOOK);
		
		S_COMM_HELP = config.getString("strings.command-help", S_COMM_HELP);
		S_COMM_GET = config.getString("strings.command-get", S_COMM_GET);
		S_COMM_ID = config.getString("strings.command-id", S_COMM_ID);
		S_COMM_READ = config.getString("strings.command-read", S_COMM_READ);
		S_COMM_TITLE = config.getString("strings.command-title", S_COMM_TITLE);
		S_COMM_AUTHOR = config.getString("strings.command-author", S_COMM_AUTHOR);
		S_COMM_UNDO = config.getString("strings.command-undo", S_COMM_UNDO);
		S_COMM_ERASE = config.getString("strings.command-erase", S_COMM_ERASE);
		S_COMM_REPLACE = config.getString("strings.command-replace", S_COMM_REPLACE);
		S_COMM_ERASEALL = config.getString("strings.command-eraseall", S_COMM_ERASEALL);
		S_COMM_CHATMODE = config.getString("strings.command-chatmode", S_COMM_CHATMODE);
		
		S_COMM_HELP_TEXT = config.getString("strings.command-help-text", S_COMM_HELP_TEXT);
		S_COMM_GET_DONE = config.getString("strings.command-get-done", S_COMM_GET_DONE);
		S_COMM_GET_FAIL = config.getString("strings.command-get-fail", S_COMM_GET_FAIL);
		S_COMM_ID_DONE = config.getString("strings.command-id-done", S_COMM_ID_DONE);
		S_COMM_ID_FAIL = config.getString("strings.command-id-fail", S_COMM_ID_FAIL);
		S_COMM_TITLE_DONE = config.getString("strings.command-title-done", S_COMM_TITLE_DONE);
		S_COMM_AUTHOR_DONE = config.getString("strings.command-author-done", S_COMM_AUTHOR_DONE);
		S_COMM_UNDO_DONE = config.getString("strings.command-undo-done", S_COMM_UNDO_DONE);
		S_COMM_UNDO_FAIL = config.getString("strings.command-undo-fail", S_COMM_UNDO_FAIL);
		S_COMM_ERASE_DONE = config.getString("strings.command-erase-done", S_COMM_ERASE_DONE);
		S_COMM_REPLACE_DONE = config.getString("strings.command-replace-done", S_COMM_REPLACE_DONE);
		S_COMM_REPLACE_FAIL = config.getString("strings.command-replace-fail", S_COMM_REPLACE_FAIL);
		S_COMM_ERASEALL_DONE = config.getString("strings.command-eraseall-done", S_COMM_ERASEALL_DONE);
		S_COMM_CHATMODE_ON = config.getString("strings.command-chatmode-on", S_COMM_CHATMODE_ON);
		S_COMM_CHATMODE_OFF = config.getString("strings.command-chatmode-off", S_COMM_CHATMODE_OFF);
		S_COMM_INVALID = config.getString("strings.command-invalid", S_COMM_INVALID);
		
		S_WRITE_DONE = config.getString("strings.write-done", S_WRITE_DONE);
		S_WRITE_FAIL = config.getString("strings.write-fail", S_WRITE_FAIL);
		
		S_READ_DIVIDER = config.getString("strings.read-divider", S_READ_DIVIDER);
		S_READ_BOOK = config.getString("strings.book", S_READ_BOOK);
		S_READ_BY = config.getString("strings.by", S_READ_BY);
		S_READ_PAGE = config.getString("strings.page", S_READ_PAGE);
		
		S_COPIED_BOOK = config.getString("strings.copied-book", S_COPIED_BOOK);
		S_REMOVED_BOOK = config.getString("strings.removed-book", S_REMOVED_BOOK);
		S_PLACED_BOOK = config.getString("strings.placed-book", S_PLACED_BOOK);
		S_PLACED_BOOK_FAIL = config.getString("strings.placed-book-fail", S_PLACED_BOOK_FAIL);
		S_NO_PERMISSION = config.getString("strings.no-permission", S_NO_PERMISSION);
	}
	
	protected void saveBookshelves() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(new File(BookWorm.plugin.getDataFolder(), "bookshelves.txt"), false));
			for (String s : bookshelves.keySet()) {
				writer.println(s + ":" + bookshelves.get(s));
			}
		} catch (IOException e) {
			getServer().getLogger().severe("BookWorm: Error writing bookshelf list");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	protected void saveExtraBookIds() {
		File file = new File(BookWorm.plugin.getDataFolder(), "extrabookids.txt");
		if (extraBookIds.size() == 0) {
			if (file.exists()) file.delete();
		} else {
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(new FileWriter(file, false));
				for (Short s : extraBookIds) {
					writer.println(s);
				}
			} catch (IOException e) {
				getServer().getLogger().severe("BookWorm: Error writing extra book id list");
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}
	}
	
	protected short getNextBookId() {
		// check in extra book id list
		if (extraBookIds != null && extraBookIds.size() > 0) {
			short id = extraBookIds.remove(0);
			saveExtraBookIds();
			return id;
		}
		
		// get next book id
		short id = (short) (getCurrentBookId() + 1);
		// save current book id to file
		if (id > 0) {
			File file = new File(getDataFolder(), "bookid.txt");
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(new FileWriter(file, false));
				writer.println(id);
			} catch (IOException e) {
				return -1;
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}
		return id;
	}
	
	protected short getCurrentBookId() {
		short id;
		File file = new File(getDataFolder(), "bookid.txt");
		if (!file.exists()) {
			id = 0;
		} else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String s = reader.readLine();
				id = Short.parseShort(s);
			} catch (Exception e) {
				id = -1;
			} finally {
				try {
					if (reader != null) reader.close();
				} catch (Exception e) {
				}
			}
		}
		return id;
	}
	
	@Override
	public void onDisable() {
		if (unloader != null) {
			unloader.stop();
		}
		for (Book book : books.values()) {
			if (!book.isSaved()) {
				book.save();
			}
		}
		books = null;
		bookshelves = null;
		bookmarks = null;
		unloader = null;
	}

}
