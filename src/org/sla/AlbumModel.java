package org.sla;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class AlbumModel implements Serializable {
    static private final long serialVersionUID = 2L;

    // CLASS FIELDS
              private static ObservableList<AlbumModel> albums = FXCollections.observableArrayList();
              private static int currentRanking = 1;
    transient private static Image avatar;

    // OBJECT FIELDS
    transient private final SimpleIntegerProperty ranking = new SimpleIntegerProperty();
    transient private final SimpleStringProperty artist = new SimpleStringProperty();
    transient private final SimpleStringProperty name = new SimpleStringProperty();;
    transient private final SimpleIntegerProperty year = new SimpleIntegerProperty();;
    transient private final SimpleStringProperty genre = new SimpleStringProperty();;
    transient private final SimpleFloatProperty certifiedSales = new SimpleFloatProperty();
    transient private final SimpleIntegerProperty claimedSales = new SimpleIntegerProperty();;

    // CONSTRUCTOR
    AlbumModel(int ranking, String artist, String name, int year, String genre, float certifiedSales, int claimedSales) {
        setRanking(ranking);
        setArtist(artist);
        setName(name);
        setYear(year);
        setGenre(genre);
        setCertifiedSales(certifiedSales);
        setClaimedSales(claimedSales);
    }

    // CLASS METHODS
    public static ObservableList<AlbumModel> getAlbums() {
        return albums;
    }

    static void importAlbums(File albumDataFile) {
        try {
            // scan data file line-by-line
            Scanner scanner = new Scanner(albumDataFile);
            while (scanner.hasNextLine()){
                String str = scanner.nextLine();
                Scanner lineScanner = new Scanner(str);
                lineScanner.useDelimiter("#");
                // scan data files line by separating text between #

                // first 4 data values are always present in each line
                String artist = lineScanner.next();
                String album = lineScanner.next();
                int year = lineScanner.nextInt();
                String genre = lineScanner.next();
                // Some lines have certified sales value, then claimed sales value
                //  But some lines have only claimed sales value, no certified sales value
                // try reading first sales amount as a float (since certified sales value is a float)
                float certifiedSales;
                int claimedSales;
                float sales1 = lineScanner.nextFloat();
                if (lineScanner.hasNextInt()) {
                    // keep certified sales value as a float
                    certifiedSales = sales1;
                    // read claimed sales value as an int
                    claimedSales = lineScanner.nextInt();
                } else {
                    // convert float to integer since claimed sales values are all integers
                    claimedSales = (int)sales1;
                    // there is no certified sales value on this line
                    certifiedSales = 0;
                }

                AlbumModel newAlbum = new AlbumModel(currentRanking, artist, album, year, genre, certifiedSales, claimedSales);
                albums.add(newAlbum);
                currentRanking = currentRanking + 1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void saveData(FileOutputStream file) {
        // serialize the program's data in this order:
        // 1. Number of albums in albums list
        // 2. Each individual album object
        // 3. The avatar image
        try {
            // Objects/values will be written to this object stream
            ObjectOutputStream out = new ObjectOutputStream(file);

            // 1. Save the number of albums in list
            out.writeInt(albums.size());
            // 2. Save each album object
            for (int i = 0; i < albums.size(); i = i + 1) {
                out.writeObject(albums.get(i));
            }
            // 3. Write a long number to create buffer between albums objects and avatar image
            out.writeLong(100L);
            // 4. Save the avatar
            ImageIO.write(SwingFXUtils.fromFXImage(avatar, null), "png", out);
            // 5. Write a long number to create buffer between avatar image and end of file
            out.writeLong(300L);

            // Done saving; close the object stream
            out.close();
        } catch (Exception ex) {
            System.out.println("Save exception: ");
            ex.printStackTrace();
        }
    }

    static void restoreData(FileInputStream file) {
        // deserialize the program's data in this order:
        // 1. Number of albums in albums list
        // 2. Each individual album object
        // 3. The avatar image
        try {
            // Objects/values will be read from this object stream
            ObjectInputStream in = new ObjectInputStream(file);

            // 1. Restore the amount of albums in list
            int numberOfAlbums = in.readInt();
            // 2. Restore each album object
            for (int i = 0; i < numberOfAlbums; i = i + 1) {
                AlbumModel nextAlbum = (AlbumModel)in.readObject();
            }
            // 3. Read and ignore the long buffer between albums objects and avatar image
            in.readLong();
            // 4. Restore the avatar
            setAvatar(SwingFXUtils.toFXImage(ImageIO.read(in), null));
            // 5. Read and ignore the long buffer between avatar image and end of file
            in.readLong();

            // Done restoring data; close the object stream
            in.close();
        } catch (Exception ex) {
            System.out.println("Restore exception: ");
            ex.printStackTrace();
        }
    }

    static void addEmptyAlbum() {
        albums.add(new AlbumModel(currentRanking, "", "", 0, "", 0.0f,0));
        currentRanking = currentRanking + 1;
    }

    static void deleteAlbum(AlbumModel album) {
        albums.remove(album);
    }

    // OBJECT METHODS
    private void writeObject(ObjectOutputStream outStream) throws IOException {
        // Do generic object write first
        outStream.defaultWriteObject();

        // Now write the 7 specific fields for an individual album object
        outStream.writeInt(getRanking());
        outStream.writeUTF(getArtist());
        outStream.writeUTF(getName());
        outStream.writeInt(getYear());
        outStream.writeUTF(getGenre());
        outStream.writeFloat(getCertifiedSales());
        outStream.writeInt(getClaimedSales());
    }

    private void readObject(ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        // Do generic object read first
        inStream.defaultReadObject();
        // Now read the 7 specific fields for an individual album object
        int ranking = inStream.readInt();
        String artist = inStream.readUTF();
        String name = inStream.readUTF();
        int year = inStream.readInt();
        String genre = inStream.readUTF();
        float certifiedSales = inStream.readFloat();
        int claimedSales = inStream.readInt();

        // Now that we read all of the individual album's data, create the album and add it to the list
        albums.add(new AlbumModel(ranking, artist, name, year, genre, certifiedSales,claimedSales));
    }

    // OBJECT GETTER/SETTER
    public static Image getAvatar() {
        return avatar;
    }

    public static void setAvatar(Image avatar) {
        AlbumModel.avatar = avatar;
    }

    public String getArtist() {
        return artist.get();
    }

    public void setArtist(String artist) {
        this.artist.set(artist);
    }

    public int getRanking() {
        return ranking.get();
    }

    public void setRanking(int name) {
        this.ranking.set(name);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getYear() {
        return year.get();
    }

    public void setYear(int year) {
        this.year.set(year);
    }

    public String getGenre() {
        return genre.get();
    }

    public void setGenre(String genre) {
        this.genre.set(genre);
    }

    public float getCertifiedSales() {
        return certifiedSales.get();
    }

    public void setCertifiedSales(float sales) {
        this.certifiedSales.set(sales);
    }

    public int getClaimedSales() {
        return claimedSales.get();
    }

    public void setClaimedSales(int sales) {
        this.claimedSales.set(sales);
    }
}
