package photo_renamer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PhotoRenamerGUI extends JFrame{

	/**
	 * The PhotoRenamerGUI constructs a GUI to be used by the user. All listener 
	 * required are defined (as private classes) inside this class.
	 * 
	 * renamer: Used to access the back-end
	 * 
	 * JFileChooser: used to let the user choose a directory
	 * 
	 * images: stores buffered images of all images in chosen directory
	 * 
	 * imageLabel: used to display the picture selected in the GUI
	 * 
	 * icon: used to display the picture selected in the GUI
	 * 
	 * all other variables are JButtons which lets the user access the features of the 
	 * program.
	 */
	private static final long serialVersionUID = -6265183077154269029L;
	private PhotoRenamer renamer;
	private final JFileChooser directoryChooser = new JFileChooser();
	private JPanel panel = new JPanel();
	private JButton addTag = new JButton("Add New Tag to Image and Set");
	private JButton selectTags = new JButton("Select Tag(s) from Tag Set");
	private JButton removeTag = new JButton("Remove Tag from Image");
	private JButton revertName = new JButton("Revert To Older Name");
	private JButton addToSet = new JButton("Add to Tag Set Only");
	private JButton removeTagSet = new JButton("Remove Tag from Set");
	private JButton viewTagSet = new JButton("view Tag Set");
	private JButton viewImageHistory = new JButton("view all historical names of image");
	private JButton viewAllHistory = new JButton("view all changes made to all images");
	private BufferedImage[] images;
	private JLabel imageLabel;
	private ImageIcon icon;
	private int width;
	private static PhotoRenamerGUI gui = new PhotoRenamerGUI();
	
	//Singleton Design Pattern (Design Pattern 2: DP2) private constructor so this 
	//class cannot be instantiated
	private PhotoRenamerGUI(){
		super("Photo Renamer");
		//lets the user choose a directory
		directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		directoryChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int rval = directoryChooser.showOpenDialog(this);
		//checks if user has chosen a directory
		if(JFileChooser.APPROVE_OPTION == rval){
			renamer = new PhotoRenamer(directoryChooser.getSelectedFile());
			
			if(renamer.getImages().length > 0){
				//sets up an image to be displayed in the gui (if the directory has images)
				//by default the first image image in the outer most directory is shown
				ArrayList<BufferedImage> images2 = new ArrayList<>();
				for(Image i: renamer.getImages()){
					try {
						images2.add(ImageIO.read(i.getImage()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				images = images2.toArray(new BufferedImage[images2.size()]);
				icon = new ImageIcon(images[0].getScaledInstance(250, 200, BufferedImage.SCALE_SMOOTH));
				imageLabel = new JLabel(null, icon, JLabel.CENTER);
				panel.add(imageLabel);
				
			}
			
			JList<Image> pictures = new JList<>(renamer.getImages());
			pictures.addListSelectionListener(new ImageDisplay(imageLabel, pictures, images, icon));
			pictures.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			//part of DP1 adds observer to several objects
			Buttons buttonListener = new Buttons(pictures, renamer);
			addTag.addActionListener(buttonListener);
			removeTag.addActionListener(buttonListener);
			selectTags.addActionListener(buttonListener);
			revertName.addActionListener(buttonListener);
			addToSet.addActionListener(buttonListener);
			removeTagSet.addActionListener(buttonListener);
			viewTagSet.addActionListener(buttonListener);
			viewImageHistory.addActionListener(buttonListener);
			viewAllHistory.addActionListener(buttonListener);
			JScrollPane scroller = new JScrollPane(pictures);
			panel.add(scroller);
			panel.add(selectTags);
		    panel.add(removeTag);
		    panel.add(revertName);
		    panel.add(addTag);
		    panel.add(addToSet);
		    panel.add(removeTagSet);
		    panel.add(viewTagSet);
		    panel.add(viewImageHistory);
		    panel.add(viewAllHistory);
		    panel.setBackground(Color.WHITE);
		    width = scroller.getWidth();
			this.add(panel);
			this.setDefaultCloseOperation(EXIT_ON_CLOSE);
			this.pack();
			this.setSize(300 + width, 700);
			this.setVisible(true);
		}else{
			//if the user hasn't chosen a directory the program shuts down
			System.exit(ABORT);
		}
	}
	 
	private class ImageDisplay implements ListSelectionListener{
		private JList<Image> pictures;
		private JLabel imageLabel;
		private BufferedImage[] image;
		private ImageIcon icon;
		
		ImageDisplay(JLabel label, JList<Image> list, BufferedImage[] image, ImageIcon icon){
			this.pictures = list;
			this.imageLabel = label;
			this.image = image;
			this.icon = icon;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			//changes which image is displayed in the gui depending on what item in the 
			//JList of pictures is selected
			icon = new ImageIcon(image[pictures.getSelectedIndex()].getScaledInstance(250, 200, BufferedImage.SCALE_SMOOTH));
			imageLabel.setIcon(icon);
		}
		
	}
	//Observer Design Pattern (Design Pattern 1: DP1) observer makes no assumption on 
	//what the observed object is.
	private class Buttons implements ActionListener{
		private JList<Image> pictures;
		private PhotoRenamer renamer;
		
		Buttons(JList<Image> pictures, PhotoRenamer renamer){
			this.pictures = pictures;
			this.renamer = renamer;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton event = (JButton) e.getSource();
			if(event.equals(addTag)){
				//if the addTag button is pressed
			    String tag = "@" + JOptionPane.showInputDialog("Pick a Tag (Tag should not contain '@' character)");
			    Image selected = pictures.getSelectedValue();
			    if(!tag.equals("@null")){
			    	try {
			    		renamer.addTag(selected, tag);
			    	} catch (ImageNotFoundException e1) {
			    		JOptionPane.showMessageDialog(panel, "Could not add tag");
			    		e1.printStackTrace();
			    	}
			    }else{
			    	return;
			    }
		    }else if(event.equals(removeTag)){
		    	//if the remove tag button is pressed
				Image selected = pictures.getSelectedValue();
				if(selected == null){
					return;
				}
				JList<String> toBeRemoved = new JList<>(selected.getCurrTags());
				JScrollPane scroller = new JScrollPane(toBeRemoved);
				scroller.setPreferredSize(new Dimension(300,125));
				JOptionPane.showMessageDialog(panel,scroller,"Select Tags to Delete", JOptionPane.PLAIN_MESSAGE);
				String[] values = toBeRemoved.getSelectedValuesList().toArray(new String[toBeRemoved.getSelectedValuesList().size()]);
				try {
					renamer.deleteMultipleTags(selected, values);
				}catch (ImageNotFoundException | TagNotFoundException e1){
					JOptionPane.showMessageDialog(panel, "Something Went Wrong");
					e1.printStackTrace();
				}
			}else if(event.equals(selectTags)){
				//if the select tag button is pressed
				Image selected = pictures.getSelectedValue();
				JList<String> toBeAdded = new JList<>(PhotoRenamer.getTagSet());
				if(toBeAdded.getSelectedValuesList().contains(null) || selected == null){
					return;
				}
				JScrollPane scroller = new JScrollPane(toBeAdded);
				scroller.setPreferredSize(new Dimension(300,125));
				JOptionPane.showMessageDialog(panel, scroller, "Select Tags", JOptionPane.PLAIN_MESSAGE);
				String[] values = toBeAdded.getSelectedValuesList().toArray(new String[toBeAdded.getSelectedValuesList().size()]);
				try {
					renamer.addMultipleTags(selected, values);
				} catch (ImageNotFoundException e1) {
					JOptionPane.showMessageDialog(panel, "Something Went Wrong");
					e1.printStackTrace();
				}
			}else if(event.equals(revertName)){
				//if the revertName button is pressed				
				Image selected = pictures.getSelectedValue();
				if(selected == null){
					return;
				}
				try {
					JList<String> olderNames = new JList<>(renamer.viewImageNames(selected));
					olderNames.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					JScrollPane scroller = new JScrollPane(olderNames);
					scroller.setPreferredSize(new Dimension(300,125));
					JOptionPane.showMessageDialog(panel, scroller, "Select Older Name", JOptionPane.PLAIN_MESSAGE);
					String name = olderNames.getSelectedValue();
					if(name == null){
						name = selected.toString();
					}
					renamer.olderName(selected, name);
				} catch (ImageNotFoundException e1) {
					JOptionPane.showMessageDialog(panel, "Something Went Wrong");
					e1.printStackTrace();
				} catch (TagNotFoundException e1) {
					JOptionPane.showMessageDialog(panel, "Something Went Wrong");
					e1.printStackTrace();
				}
			}else if(event.equals(addToSet)){
				//if the addToSet button is pressed
				String tag = "@" + JOptionPane.showInputDialog("Pick a Tag");
			    if(!tag.equals("@null")){
			    	PhotoRenamer.addToSet(tag);
			    }else{
			    	return;
			    }
			}else if(event.equals(removeTagSet)){
				//if the removeTagSet button is pressed
				JList<String> toBeRemoved = new JList<>(PhotoRenamer.getTagSet());
				JScrollPane scroller = new JScrollPane(toBeRemoved);
				scroller.setPreferredSize(new Dimension(300,125));
				JOptionPane.showMessageDialog(panel, scroller, "Select Tags to Remove", JOptionPane.PLAIN_MESSAGE);
				String[] values = toBeRemoved.getSelectedValuesList().toArray(new String[toBeRemoved.getSelectedValuesList().size()]);
				try{
					for(String v: values){
						if(v == null){
							return;
						}
						PhotoRenamer.deleteFromSet(v);
					}
				}catch(TagNotFoundException e1){
					JOptionPane.showMessageDialog(panel, "Something Went Wrong");
					e1.printStackTrace();
				}
			}else if(event.equals(viewTagSet)){
				//if the viewTagSet button is pressed
				JTextArea tags = new JTextArea();
				tags.setEditable(false);
				tags.setText(PhotoRenamer.viewTagSet());
				JScrollPane scroller = new JScrollPane(tags);
				scroller.setPreferredSize(new Dimension(300,125));
				JOptionPane.showMessageDialog(panel, scroller);
			}else if(event.equals(viewImageHistory)){
				//if the viewImageHistory button is pressed
				JTextArea names = new JTextArea();
				names.setEditable(false);
				Image selected = pictures.getSelectedValue();
				if(selected == null){
					return;
				}
				try {
					names.setText(renamer.viewImageLog(selected));
					JScrollPane scroller = new JScrollPane(names);
					scroller.setPreferredSize(new Dimension(300,125));
					JOptionPane.showMessageDialog(panel,scroller);
				} catch (ImageNotFoundException e1) {
					JOptionPane.showMessageDialog(panel, "something went horribly wrong");
					e1.printStackTrace();
				}
			}else if(event.equals(viewAllHistory)){
				//if the view all history button is pressed.
				JTextArea history = new JTextArea();
				history.setEditable(false);
				history.setText(PhotoRenamer.getAllHistory());
				JScrollPane scroller = new JScrollPane(history);
				scroller.setPreferredSize(new Dimension(300,125));
				JOptionPane.showMessageDialog(panel, scroller);
			}
		}
	}
	//part of DP2. (global point of access) 
	public static PhotoRenamerGUI run(){
		return gui;
	}
}
