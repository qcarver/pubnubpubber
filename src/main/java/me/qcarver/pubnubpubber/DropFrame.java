/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.qcarver.pubnubpubber;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Quinn Carver
 */
public class DropFrame extends JFrame implements DropTargetListener {
    
    JLabel label;
    String title;
    private Object lock = new Object();
    
    public interface DropCallback{
        public void onDrop(String nameOfFileDropped);
    }
    
    private DropCallback dropCallback;

    
    DropFrame(String title, String labelContents, DropCallback dropCallback){
                this.dropCallback = dropCallback;
                this.title = title;
                label = new JLabel(labelContents);
                initJframe();
    }

    DropTarget dt = new DropTarget(
            this,
            DnDConstants.ACTION_COPY_OR_MOVE,
            this,
            true);
    
    private void initJframe() {
        //make sure to shut down the application, when the frame is closed
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setTitle("PubNubPubber");

        setResizable(true);
        setLocation(10, 10);
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(275, 225));
        panel.add(label);
        getContentPane().add(panel);
        pack();
        setVisible(true);
        System.err.println("finished init");
        Thread t = new Thread() {
            public void run() {
                synchronized (lock) {
                    while (isVisible()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Window closing");
                }
            }
        };
        t.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                synchronized (lock) {
                    setVisible(false);
                    lock.notify();
                }
            }
        });

        try {
            t.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Drag and Drop stuff
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        //to do show a hint to user.. maybe start getting the name and parsing?
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        String fileName = getFileNameDraggedEvent(dtde);
        File file = new File(fileName);
        if (file != null) {
            if (!file.isDirectory()) {
                System.out.println("Name of file dragged in is: " + file);
                //publishFile(file.getAbsolutePath());
                dropCallback.onDrop(file.getAbsolutePath());
                //send file contents
            }
        }
    }

    /**
     * Thanks to "Rocket Hazmat" on Stack Overflow for this gem. Behavior in
     * Windows different than Mac/Linux, so DnD impl is not straight fwd!
     *
     * @param dtde
     * @return fileName or empty if not a file/didn't-work
     */
    private String getFileNameDraggedEvent(DropTargetDropEvent dtde) {
        String filename = "";

        try {
            // Get the object to be transferred
            Transferable tr = dtde.getTransferable();
            DataFlavor[] flavors = tr.getTransferDataFlavors();

            // If flavors is empty get flavor list from DropTarget
            flavors = (flavors.length == 0) ? dtde.getCurrentDataFlavors() : flavors;

            // Select best data flavor
            DataFlavor flavor = DataFlavor.selectBestTextFlavor(flavors);

            // Flavor will be null on Windows
            // In which case use the 1st available flavor
            flavor = (flavor == null) ? flavors[0] : flavor;

            // Flavors to check
            DataFlavor Linux = new DataFlavor("text/uri-list;class=java.io.Reader");
            DataFlavor Windows = DataFlavor.javaFileListFlavor;

            // On Linux (and OS X) file DnD is a reader
            if (flavor.equals(Linux)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                BufferedReader read = new BufferedReader(flavor.getReaderForText(tr));
                // Remove 'file://' from file name
                filename = read.readLine().substring(7).replace("%20", " ");
                // Remove 'localhost' from OS X file names
                if (filename.substring(0, 9).equals("localhost")) {
                    filename = filename.substring(9);
                }
                read.close();
                dtde.dropComplete(true);
                System.out.println("File Dragged:" + filename);
                //mainWindow.openFile(fileName);
            } // On Windows file DnD is a file list
            else if (flavor.equals(Windows)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                @SuppressWarnings("unchecked")
                List<File> list = (List<File>) tr.getTransferData(flavor);
                dtde.dropComplete(true);

                if (list.size() == 1) {
                    System.out.println("File Dragged: " + list.get(0));
                    filename = list.get(0).getAbsolutePath(); //<-Mac was here!?
                }
            } else {
                System.err.println("DnD Error");
                dtde.rejectDrop();
            }
            return filename;
        } //TODO: OS X Throws ArrayIndexOutOfBoundsException on first DnD
        catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("DnD not initalized properly, please try again.");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (UnsupportedFlavorException e) {
            System.err.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
        return filename;
    }
    
    void showError(String errorMessage){
        label.setForeground(Color.red);
        label.setText(errorMessage);
    }
}
