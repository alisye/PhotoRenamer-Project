package photo_renamer;
import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class Image implements Serializable{
	
	/**
	 * The Image class keeps track of a image file's name and location, it can
	 * add and delete tags to an image. It can also report all the tags an image 
	 * has and when these tags were added. This class is to be used only by PhotoRenamer.
	 * 
	 * serialVersionUID: A constant created by eclipse for this class to be Serializeable
	 * tags: A list of all current tags this image has
	 * name: The name of this image
	 * location: The directory in which this image is stored
	 * image: The file of this image
	 * log: The TagLog associated with this image
	 */
	private static final long serialVersionUID = 733556395704688L;
	private ArrayList<String> curTags = new ArrayList<>();
	private String name;
	private String location;
	private File image;
	private TagLog log;
	
	/**
	 * Instantiates the image
	 * 
	 * @param name: the name of this image
	 * @param image: the file of this image
	 * @param log: the TagLog that will keep track of this image's names
	 */
	
	Image(String name, File image, TagLog log){
		this.name = name;
		this.image = image;
		this.location = image.getPath();
		this.log = log;
		logAdder(this.name);
	}
	/**
	 * Gives the file associated with this image class
	 * 
	 * @return the File of the image
	 */
	public File getImage() {
		return image;
	}
	/**
	 * Gives the name of this image
	 * 
	 * @return the name of this image
	 */
	@Override
	public String toString() {
		return name;
	}
	/**
	 *Gives the all the names the image has ever had and what time they were created
	 *
	 * @return all names and creation times
	 */
	public String nameLog() {
		return this.log.toString();
	}
	/**
	 * Gives all names this image has ever had
	 * 
	 * @return All names this image has had
	 */
	public String[] allNames(){
		return this.log.viewAllTags().toArray(new String[this.log.viewAllTags().size()]);
	}
	/**
	 * adds the given tag to this image. Renames the image to include this Tag.
	 * 
	 * @param Tag: the tag to be added to this image
	 */
	public void addTag(String Tag) {
		//modifies the name of the image to include the tag
		this.name = this.name.substring(0, this.name.lastIndexOf(".")) + 
				Tag + this.name.substring(this.name.lastIndexOf("."),
				this.name.length());
		curTags.add(Tag);
		//if the modified name is new it adds it to the log
		if (!log.contains(this.name)){
			logAdder(this.name);
		}
		//changes the file name to include the tag
		this.image.renameTo(new File(this.location.substring(0, this.location.lastIndexOf("/")) + "/" + this.name));
		this.image = new File(this.location.substring(0, this.location.lastIndexOf("/")) + "/" + this.name);
		this.location = this.image.getPath();
	}
	/**
	 * Deletes a currently existing tag from this image
	 * 
	 * @param Tag: the tag to be deleted form this image
	 * @throws TagNotFoundException: this exception is thrown if the tag to be deleted
	 * is not a tag of this image
	 */
	public void deleteTag(String Tag) throws TagNotFoundException{
		//check if the image has the tag
		if(!(curTags.contains(Tag))){
			throw new TagNotFoundException();
		}
		int index = this.curTags.indexOf(Tag);
		// Assume the there is no @ in initial name. get the name of the original image
		// (with no tags) 
		String initialName = this.name.substring(0,this.name.indexOf("@"));
		// get the extension
		String ext = this.name.substring(this.name.lastIndexOf("."), this.name.length());
		//remove the tag from the current tag list 
		this.curTags.remove(index);
		//add all the current tags back to the initial name
		for (String i: this.curTags){
			initialName += i;
		}
		//set the name of the image to it's initial name (with tags) and the extension
		this.name = initialName + ext;
		//change the name of the file itself
		this.image.renameTo(new File (this.location.substring(0, this.location.lastIndexOf("/")) 
				+ "/" + this.name));
		this.image = new File(this.location.substring(0, this.location.lastIndexOf("/")) + "/" + this.name);
		this.location = this.image.getPath();
		if (!this.log.contains(this.name)){
			logAdder(this.name);
		}
	}
	/**
	 * Turns the name of this image back into a previous name this image has had
	 * 
	 * @param name: the name to revert to
	 * @throws TagNotFoundException: this exception is thrown if this image has never
	 * had the name to revert to
	 */
	public void revertOlderName(String name) throws TagNotFoundException{
		//check if given name is actually an older name
		if(!(this.log.contains(name))){
			throw new TagNotFoundException();
		}
		//set the name of the image to it's older name
		this.name = name;
		//assume tags do not contain "@" character
		if(name.contains("@")){
			//if the older image name has tags add those tags to the image.
			ArrayList<Integer> indexes = new ArrayList<>();
			for(int i=0; i < name.toCharArray().length; i++){
				if(name.toCharArray()[i] == '@'){
					indexes.add(i);
				}
			}
			curTags.clear();
			for(int i=0; i < indexes.size(); i++){
				if(!(i == indexes.size()-1)){
					curTags.add(name.substring(indexes.get(i), indexes.get(i+1)));
				}else{
					curTags.add(name.substring(indexes.get(i), name.indexOf('.')));
				}
			}
		}
		//change the name of the file itself
		this.image.renameTo(new File(this.location.substring(0, this.location.lastIndexOf("/")) + "/" + name));
		this.image = new File(this.location.substring(0, this.location.lastIndexOf("/")) + "/" + name);
		this.location = this.image.getPath();
	}
	/**
	 * gives a string array of all the tags this image has currently
	 * 
	 * @return: a string array of tags this image has
	 */
	public String[] getCurrTags(){
		return curTags.toArray(new String[curTags.size()]);
	}
	/**
	 * A helper function used to add names to the TagLog
	 * 
	 * @param tag: the name to be added to the TagLog
	 */
	private void logAdder(String tag){
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		this.log.addTag(tag, dateFormat.format(cal.getTime()));
	}
	
}
