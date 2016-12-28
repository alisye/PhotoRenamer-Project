package photo_renamer;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;


public class TagLog implements Serializable{
	
	/**
	 * The TagLog keeps track of all names an image has ever had and what time that 
	 * name was created. Each Image is associated with it's own TagLog.
	 * 
	 * serialVersionUID: a constant long created by eclipse to make TagLog Serializable
	 * 
	 * name: A map of all names for a particular image, the key is the name of the image
	 * and the values are the time the image was made. 
	 */
	private static final long serialVersionUID = 5544347005324932220L;
	private HashMap<String, String> name = new HashMap<>();
	
	/**
	 * Shows all names the image associated with this TagLog has ever had
	 *
	 * @return: A collection of all all names the image has had
	 */
	public Collection<String> viewAllTags(){
		return this.name.keySet();
	}
	/**
	 * Adds a new name, and the time the name was created to the TagLog
	 * 
	 * @param name: The new name to be added to the TagLog
	 * @param time: The time this new name was created
	 */
	public void addTag(String name, String time){
		this.name.put(name, time);
	}
	/**
	 * Shows what time a particular name for an image was created
	 * 
	 * @param name: the name of the image you want to find the creation time of
	 * @return the time the name was created
	 */
	public String getTime(String name){
		return this.name.get(name);
	}
	/**
	 * Tells the user if an image has ever had a particular name
	 * 
	 * @param name: the name to check if the image ever had
	 * @return true if the image has had this name false if the image has not had this name
	 */
	public boolean contains(String name){
		return this.name.containsKey(name);
	}
	/**
	 * returns a string representation of all names and the associated time the tracked 
	 * image was renamed.
	 * 
	 * @return a string representation of all names and the associated time the tracked 
	 * image was renamed.
	 */
	@Override
	public String toString(){
		String s = "";
		for (String keys: this.name.keySet()){
			s += String.format("name: %s , time: %s" + "\n", keys, this.name.get(keys));
		}
		return s;
	}
}
