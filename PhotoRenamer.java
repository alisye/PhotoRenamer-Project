package photo_renamer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.activation.MimetypesFileTypeMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PhotoRenamer{

	private File directory;
	
	ArrayList<Image> imageList = new ArrayList<>();
	private final static Logger logger = Logger.getLogger(PhotoRenamer.class.getName());
	private final static File logFile = new File(System.getProperty("user.home") + "/Library/PhotoRenamer/logFile.txt");
	//Set up for MacOS X may need to adjust for different OS
	private static Handler filehandler;
	private static ArrayList<String> tagSet = new ArrayList<>();
	private final static File tagSetFile = new File(System.getProperty("user.home") + "/Library/PhotoRenamer/.tagSet.ser");
	//Set up for MacOS X may need to adjust for different OS
	private final static File storage = new File(System.getProperty("user.home") + "/Library/PhotoRenamer");
	//Set up for MacOS X may need to adjust for different OS
	/**
	 * Reads a given directory and stores all the image files in that directory. This
	 * class can add, delete, and view, tags of any of the images in the directory. This
	 * class will interact directly with the GUI
	 * 
	 * imageList: stores all images in given directory
	 * 
	 * logger: logs all renaming (adding, delecting and reverting) PhotoRename does
	 * 
	 * logFile: A text file which the filehandler writes to.
	 * 
	 * filehandler: the filehandler gets reports from the logger when an image is 
	 * renamed and writes to file to record this renaming
	 * 
	 * tagSet: the set of all tags for all images
	 * 
	 * tagSetFile: the file where the tag set is serialized (so it persists) 
	 * 
	 * storage: the directory where all files used by the PhotoRenamer is stored. 
	 */
	
	/** 
	 * Instantiates the PhotoRenamer class, stores all images in the directory into imageList
	 * checks if a file named ".images.ser" exist in the given directory if the file does
	 * not exist it is created if it does exist the constructor reads the file and stores
	 * the ArrayList contained in that file as imageList. ".images.ser" stores serialized 
	 * ArrayLists of images.
	 * 
	 * @param directory
	 */
	public PhotoRenamer(File directory){
		//check if file exits (i.e. checks if this is the first time this program is being
		//used) if this is the first time the program creates a directory to store
		//it's files
		if(!(storage.exists())){
			storage.mkdir();
		}
		this.directory = directory;
		//checks if a log file exits to record all activities done by the photorenamer
		//if it deons't exist it is created
		if(!(logFile.exists())){
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				System.out.println("could not create log file");
				e.printStackTrace();
			}
		}
		//sets up the logging system
		try {
			filehandler = new FileHandler(logFile.getPath(), true);
			logger.setLevel(Level.ALL);
			filehandler.setLevel(Level.ALL);
			logger.addHandler(filehandler);
		} catch (SecurityException | IOException e1) {
			System.out.println("could not create file handler");
			e1.printStackTrace();
		}
		//sets up the serializable file if this program is being run for the first time
		if(!(Arrays.asList(directory.listFiles()).contains(new File(directory.getPath() + "/" + ".images.ser")))){;
			File serFile = new File(directory.getPath() + "/" + ".images.ser");
			try {
				serFile.createNewFile();
				checkDirectory(directory, null);
				serializeImageList();
			}
			catch (IOException e) {
			    System.out.println("Serializable file could not be created");
				e.printStackTrace();
			}
		//if this program is not being ran for the first gets tag created in previous 
		//session
		}else {
			deSerializeImageList();
			ArrayList<File> imageFiles = new ArrayList<>();
			for (Image images: this.imageList){
				imageFiles.add(images.getImage());
			}
			checkDirectory(directory, imageFiles);
		}
		if(!(tagSetFile.exists())){ 
			try {
				tagSetFile.createNewFile();
				serializeTagSet();
			}catch (IOException e){
				System.out.println("Serializable file could not be created");
				e.printStackTrace();
			}
		}else {
			deSerializeTagSet();
		}
	}
	/**
	 * Adds the given tag to the given image in the directory and adds the tag 
	 * into the tag set if the tag isn't already in the set.
	 * 
	 * @param imageName: the image to add the tag to.
	 * @param tagName: the tag to add to the image.
	 * @throws ImageNotFoundException: thrown if the image does not exist in the directory.
	 */
	public void addTag(Image imageName, String tagName) throws ImageNotFoundException{
		//check if image is in directory if so change the name if not throw a exception
		if(imageList.contains(imageName)){
			if(!(tagSet.contains(tagName))){
				PhotoRenamer.addToSet(tagName);
				}
			imageName.addTag(tagName);
			serializeImageList();
			//log as fine if the tag was successfully added
			logger.log(Level.FINE, String.format("Tag %s added to image %s", tagName, imageName));
		}else {
			//log as sever if the tag was not added
			logger.log(Level.SEVERE, "tried to add tag for Image not in directory", new ImageNotFoundException());
			throw new ImageNotFoundException();
		}
	}
	/**
	 * Adds multiple tags to a given image in the directory. 
	 * 
	 * @param imageName: the image to add the tags to
	 * @param tagName: the tags which to add
	 * @throws ImageNotFoundException: thrown if the image is not in the directory
	 */
	public void addMultipleTags(Image imageName, String ... tagName) throws ImageNotFoundException{
		//goes throw the tags given and adds them one by one using the addTag method
		//if the image is not in the directory an exception is thrown
		for (int i=0; i < tagName.length; i++){
			this.addTag(imageName, tagName[i]);
		}
	}
	/**
	 * Deletes the given tag from the given image in the directory.
	 * 
	 * @param imageName: The image whose tag should be deleted.
	 * @param tagName: The tag that should be deleted from the image.
	 * @throws ImageNotFoundException: Thrown if the image does not exist in the directory
	 * @throws TagNotFoundException: Thrown if the Tag does not exist in the image.
	 */
	public void deleteTag(Image imageName, String tagName) throws ImageNotFoundException, TagNotFoundException{
		//checks if image is in directory
		if(imageList.contains(imageName)){
			try {
				imageName.deleteTag(tagName);
				logger.log(Level.FINE, String.format("deleted tag %s from image %s", tagName, imageName));
				//if tag is deleted successfully it is logged as fine
			} catch (TagNotFoundException e) {
				//if the tag isn't deleted it means that the image doesn't have that tag
				//so an exception is thrown and logged as severe 
				logger.log(Level.SEVERE, "Tag does not exist for image", e);
				throw e;
			}
			serializeImageList();
		}else {
			//exception is thrown if image is not in directory
			//and logged as severe
			logger.log(Level.SEVERE, "Tried to delete from image not in directory", new ImageNotFoundException());
			throw new ImageNotFoundException();
		}
	}
	/**
	 * deletes each of the given tags to the given image.
	 * 
	 * @param imageName: image to delete tags form
	 * @param tagName: tags to delete
	 * @throws ImageNotFoundException: thrown if image is not in directory
	 * @throws TagNotFoundException: thrown if the image doesn't contain a given tag
	 */
	public void deleteMultipleTags(Image imageName, String ... tagName) throws ImageNotFoundException, TagNotFoundException{
		for (int i=0; i < tagName.length; i++){
			this.deleteTag(imageName, tagName[i]);
		}
	}
	/**
	 * gives an array of all images in the directory.
	 * 
	 * @return: an array of all images in the directory.
	 */
	public Image[] getImages(){
		return this.imageList.toArray(new Image[imageList.size()]);
	}
	/**
	 * Shows all the past names of a given image and the times these past names were created.
	 * 
	 * @param imageName: the image whose past names are to be viewed
	 * @return A string representation of the past names and times these names were created of
	 * a given image.
	 * @throws ImageNotFoundException: thrown if the image does not exist in the directory.
	 */
	public String viewImageLog(Image imageName) throws ImageNotFoundException{
		//checks if image is in directory
		if(imageList.contains(imageName)){
			return imageName.nameLog();
		}else{
			throw new ImageNotFoundException();
		}
	}
	/**
	 * returns an array of all names that a given image (in the directory) has ever had
	 * 
	 * @param imageName: the image whose past names is to be returned 
	 * @return an array strings containing all past names.
	 * @throws ImageNotFoundException: thrown if the image is not in the directory.
	 */
	public String[] viewImageNames(Image imageName) throws ImageNotFoundException{
		//checks if image is in directory
		if(imageList.contains(imageName)){
			return imageName.allNames();
		}else{
			throw new ImageNotFoundException();
		}
	}
	
	/**
	 * Revert to an older name of one of the images in the directory
	 * 
	 * @param imageName: the image whose name will be reverted
	 * @param name: the name which to revert to.
	 * @throws TagNotFoundException: Thrown if the image never had the given name
	 * @throws ImageNotFoundException: Thrown if the image is not in the directory.
	 */
	public void olderName(Image imageName, String name) throws ImageNotFoundException, TagNotFoundException{
		//check if image is in directory
		if(imageList.contains(imageName)){
			//removes the image from imageList (which keeps track of images in directory
			//then change the name and put the image (now with it's name changed) back
			//into the list
			imageList.remove(imageName);
			try {
				imageName.revertOlderName(name);
			}catch (TagNotFoundException e) {
				logger.log(Level.SEVERE, "tried to change to older name with a Tag that does not exist", e);
				throw e;
			}
			imageList.add(imageName);
			logger.log(Level.FINE, String.format("changed image %s to older name %s", imageName, name));
			serializeImageList();
		}else{
			logger.log(Level.SEVERE, "tired to change to name of an image not in directory", new ImageNotFoundException());
			throw new ImageNotFoundException();
		}
	}
	/**
	 * A helper method which serializes (writes to file) the list of images
	 * 
	 */
	private void serializeImageList(){
		try {
			FileOutputStream fout = new FileOutputStream(this.directory.getPath() + "/" + ".images.ser");
			ObjectOutputStream oout = new ObjectOutputStream(fout);
			oout.writeObject(this.imageList);
			fout.close();
			oout.close();
		}catch (IOException e){
			System.out.println("serialization failed");
			e.printStackTrace();
		}
	}
	/**
	 * A helper method which deserealizes (reads form file) the list of images
	 * 
	 */
	@SuppressWarnings("unchecked") //will always deSerialze an ArrayList of images
	private void deSerializeImageList(){
		try {
			FileInputStream fout = new FileInputStream(this.directory.getPath() + "/" + ".images.ser");
			ObjectInputStream oin = new ObjectInputStream(fout);
			this.imageList = (ArrayList<Image>) oin.readObject();
			fout.close();
			oin.close();
			
		}catch (IOException | ClassNotFoundException e){
			System.out.println("could not deserialize");
			e.printStackTrace();
		}
	}
	/**
	 * a helper method which serializes the set of all tags
	 */
	private static void serializeTagSet(){
		try {
			FileOutputStream fout = new FileOutputStream(tagSetFile.getPath());
			ObjectOutputStream oout = new ObjectOutputStream(fout);
			oout.writeObject(tagSet);
			fout.close();
			oout.close();
		}catch (IOException e){
			System.out.println("could not serialize");
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked") //will always deserialize an ArrayList of strings
	private static void deSerializeTagSet(){
		try {
			FileInputStream fin = new FileInputStream(tagSetFile.getPath());
			ObjectInputStream oin = new ObjectInputStream(fin);
			tagSet = (ArrayList<String>) oin.readObject();
			fin.close();
			oin.close();
		}catch (IOException | ClassNotFoundException e){
			System.out.println("could not deserialize");
			e.printStackTrace();
		}
	}
	/**
	 * A helper method which checks if the given file is an image.
	 * 
	 * @param image: the file which to check is or is not an image
	 * @return true if the file is an image false otherwise
	 */
	private boolean imageChecker(File image){
		String type = new  MimetypesFileTypeMap().getContentType(image);
		String imagetype = type.substring(0, type.lastIndexOf("/"));
		return imagetype.equals("image");
	}
	/**
	 * A helper method which checks weather the given file is a directory; 
	 * if it is not a directory this method adds the file to the image list if 
	 * the file is an image; if it is a directory then this method recursively call 
	 * itself on the file.
	 * 
	 * @param directory: A file which if it is a directory to add all 
	 * image files in the file into the image list; and if it is not a directory
	 * it adds the file to the fileList if the file is an image
	 */
	private void checkDirectory(File directory, ArrayList<File> imageFiles){
		for (File f: directory.listFiles()){
			if(imageFiles == null){
				if (imageChecker(f)){
					this.imageList.add(new Image(f.getName(), f, new TagLog()));
				}
				else if (f.isDirectory()){
					checkDirectory(f, null);
				}
			}else {
				if (imageChecker(f) && !(imageFiles.contains(f))){
					this.imageList.add(new Image(f.getName(), f, new TagLog()));
				}
				else if (f.isDirectory()){
					checkDirectory(f, imageFiles);
			}
     	 }
	  }
	}
	/**
	 * Reads the log.txt file that the logger logged to and returns a string 
	 * representation of all renaming ever done to all images by the PhotoRenamer.
	 * 
	 * @return a String representation of all renaming ever done to all images.
	 */
	public static String getAllHistory(){
		String s = "";
		try {
			Scanner scanner = new Scanner(new FileInputStream(logFile.getPath()));
			while(scanner.hasNextLine()){
				if(scanner.nextLine().contains("<thread>") || scanner.nextLine().contains("<record>")){
					s += scanner.nextLine();
					s += "\n";
				}
			}
			scanner.close();
			return s;
		} catch (FileNotFoundException e) {
			System.out.println("log file does not exist");
			e.printStackTrace();
			return s;
		}
		
	}
	/**
	 * adds a tag into the tag set only (this does not rename an image).
	 * 
	 * @param tag: the tag that is to be added to the tag set
	 */
	public static void addToSet(String tag){
		tagSet.add(tag);
		serializeTagSet();
	}
	/**
	 * deletes a tag from the tag set (this only deletes from the set not from any
	 * images).
	 * 
	 * @param tag: the tag to be deleted from the set
	 * @throws TagNotFoundException: thrown if the given tag is not in the set
	 */
	public static void deleteFromSet(String tag) throws TagNotFoundException{
		if(tagSet.contains(tag)){
			tagSet.remove(tag);
			serializeTagSet();
		}else{
			throw new TagNotFoundException();
		}
	}
	/**
	 * shows a string representation of all tags in the tag set
	 * 
	 * @return String of all tags 
	 */
	public static String viewTagSet(){
		String s = "";
		for(String i: tagSet){
			s += i + System.lineSeparator();
		}
		return s;
	}
	/**
	 * returns a string array of all tags in the tag set.
	 * 
	 * @return a string array of all tags in the tag set.
	 */
	public static String[] getTagSet(){
		return tagSet.toArray(new String[tagSet.size()]);
	}
	public static void main(String[] args){
		PhotoRenamerGUI.run();
	}
}
