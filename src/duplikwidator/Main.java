package duplikwidator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Klasa główna programu.
 *
 */
public class Main extends javax.swing.JFrame {

    //final private String working_dir = System.getProperty("user.dir");
    static SQLite sql = new SQLite();
    static CMD cmd = new CMD();
    DefaultListModel<String> model1 = new DefaultListModel<>();
    DefaultListModel<String> model2 = new DefaultListModel<>();
    List<String> dodawanie_folderow;
    List<String> dodawanie_folderow2;
    List<Integer> usuwanie_duplikatow;
    ArrayList<String[]> dodawanie_duplikatow;
    ArrayList<String> ab_ba;

    List<String> usuwanie_duplikatow2 = new ArrayList<String>();
    List<String> pozostawione = new ArrayList<String>();

    HashMap<String, RadialHash> jpHash_hashe = new HashMap<String, RadialHash>();
    HashMap<String, String> rozdzielczosci = new HashMap<String, String>();

    HashMap<String, String> imagePHash_hashe = new HashMap<String, String>();

    HashMap<String, String> imgDiff_wyniki = new HashMap<String, String>();

    Map<String, String[]> duplikaty_elementy = new HashMap<String, String[]>();
    List<String> elementy = new ArrayList<String>();

    double stopien_procent = 95.00;
    double f1_x = 0;
    int tryb_konsoli = 0;
    double f1_y = 0;
    Connection polaczenieDB;

    public String tryb = "";
    int licznik_global;

    double f1_w = 0;
    int zakoncz_watek_zmienna = 0;
    public Thread task_1;
    DefaultTableModel model_tabeli;

    /**
     * Sprawdzanie sumy konrolnej CRC
     *
     * Metoda sprawdza wartość sumy konrolnej CRC wskazanego pliku graficznego
     *
     * @param sciezka ścieżka bezwzględna do pliku graficznego
     * @return wynik wartość sumy kontrolnej pliku graficznego
     */
    public long sprawdzCRC(String sciezka) throws IOException {

        InputStream inputStream = new BufferedInputStream(new FileInputStream(sciezka));

        CRC32 crc = new CRC32();

        int cnt;

        while ((cnt = inputStream.read()) != -1) {

            crc.update(cnt);

        }
        inputStream.close();
        long wynik = crc.getValue();
        return wynik;
    }

    /**
     * Wyświetlenie komunikatu o błędzie
     *
     * Metoda wyświetla okienko z komunikatem o błędzie
     *
     * @param message treść komunikatu
     */
    public void blad(String message) {
        final JFrame frame1 = new JFrame();
        JOptionPane.showMessageDialog(frame1,
                message, "Wystąpił błąd!", JOptionPane.ERROR_MESSAGE);

    }

    /**
     * Wyświetlenie komunikatu z informacją
     *
     * Metoda wyświetla okienko z informacją
     *
     * @param message treść komunikatu
     */
    public void komunikat(String message) {

        final JFrame frame1 = new JFrame();
        JOptionPane.showMessageDialog(frame1,
                message, "Komunikat", JOptionPane.INFORMATION_MESSAGE);

    }

    /**
     * Zakończenie działającego wątku aplikacji
     *
     * Metoda przerywa działanie wątku aplikacji i dodaje informacje uzyskane w
     * wyniku jego działania do odpowiednich tabeli lub list.
     *
     */
    public void zakoncz_watek() {

        zakoncz_watek_zmienna = 1;
        if (tryb_konsoli == 0) {
            this.setEnabled(true);
            jDialog1.dispose();
        } else {

            if (tryb.equals("Dodawanie plików z folderów")) {
                tryb = "";
                for (String plik : dodawanie_folderow) {

                    model1.addElement(plik);

                }
                //    poprawnosc();
            }

            if (tryb.equals("Dodawanie folderów")) {
                tryb = "";
                for (String sciezka : dodawanie_folderow2) {

                    model2.addElement(sciezka);

                }
                //poprawnosc();
            }

            if (tryb.equals("Dodawanie duplikatów")) {
                tryb = "";
                for (String[] array : dodawanie_duplikatow) {

                    model_tabeli.addRow(new Object[]{array[0], array[1], array[2], array[3] + "%"});

                }

            }

            if (tryb.equals("Usuwanie duplikatów")) {
                int licznik = 0;

                //  System.out.println(usuwanie_duplikatow2.toString());
                int rowy = model_tabeli.getRowCount();

                for (int licznik_row = 0; licznik_row < rowy; licznik_row++) {

                    String p1 = model_tabeli.getValueAt(licznik_row - licznik, 0).toString();
                    String p2 = model_tabeli.getValueAt(licznik_row - licznik, 1).toString();

                    if (usuwanie_duplikatow2.contains(p2) || usuwanie_duplikatow2.contains(p1)) {

                        model_tabeli.removeRow(licznik_row - licznik);

                        licznik++;
                    }

                }
                usuwanie_duplikatow2.clear();

            }

            System.out.println("Operacja zakończona.");

        }

    }

    /**
     * Sprawdzenie poprawności wypełnionych pól w oknie aplikacji.
     *
     * Metoda sprawdza, czy zostały podane wszystkie informacje wymagane do
     * wyszukania duplikatów graficznych (zaznazono odpowiednie checkboxy,
     * określono minimalny stopień podobieństwa i podano ścieżki do plików
     * graficznych)
     *
     * @return wartość logiczna: prawda lub fałsz
     */
    public boolean poprawnosc() {

        int cel = 4;
        int punkty = 0;

        if (!model1.isEmpty()) {
            punkty = punkty + 1;
        }
        if (!model2.isEmpty()) {
            punkty = punkty + 1;
        }
        if (jCheckBox1.isSelected() || jCheckBox3.isSelected() || jCheckBox4.isSelected()) {
            punkty = punkty + 1;
        }

        String stopien = jTextField1.getText();
        stopien = stopien.trim();
        stopien = stopien.replace(",", ".");
        if (!stopien.isEmpty()) {

            try {
                stopien_procent = Double.parseDouble(stopien);
                if (stopien_procent >= 0.00 && stopien_procent <= 100.00) {
                    punkty = punkty + 1;
                }
            } catch (Exception e) {

            }

        }

        if (punkty == cel) {

            jButton1.setEnabled(true);
            return true;
        } else {
            jButton1.setEnabled(false);
            return false;
        }

    }

    /**
     * Podgląd pliku graficznego
     *
     * Metoda umożliwia porównanie plików graficznych poprzez wyświetlenie ich
     * podlądu w dwóch osobnych okienkach. Jeżeli szerokość lub wysokość obrazu
     * przekracza 500 pikseli, wyświetlony obraz zostaje przeskalowany.
     *
     * @param sciezka bezwzględna ścieżka do pliku graficznego
     * @param tryb kolejność wyświetlanych plików
     */
    public void podglad(String sciezka, String tryb) {

        try {
            BufferedImage bi = ImageIO.read(new File(sciezka));

            double szer = bi.getWidth();
            double wys = bi.getHeight();

            if (szer == wys) {

                if (wys > 500) {

                    wys = 500;
                    szer = 500;

                }

            } else {

                if (wys > szer) {

                    if (wys >= 500) {

                        double roznica = wys - 500;
                        double skala = roznica / wys;
                        skala = 1 - skala;
                        wys = 500;
                        szer = szer * skala;
                    }

                } else {

                    if (szer >= 500) {

                        double roznica = szer - 500;
                        double skala = roznica / szer;
                        skala = 1 - skala;
                        szer = 500;
                        wys = wys * skala;
                    }

                }

            }

            final double x = szer;
            final double y = wys;

            final JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            final JPanel panel = new JPanel() {
                protected void paintComponent(Graphics g) {
                    Graphics g2 = g.create();

                    g2.drawImage(bi, 0, 0, (int) x, (int) y, null);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension((int) x, (int) y);
                }
            };
            frame.setResizable(false);
            //  frame.setMaximumSize(new Dimension((int) x, (int) y));
            //frame.setMinimumSize(new Dimension((int) x, (int) y));
            // frame.setPreferredSize(new Dimension((int) x, (int) y));
            frame.add(panel);
            frame.pack();

            if (tryb.equals("Pierwszy plik")) {
                frame.setTitle(tryb);
                frame.setLocationRelativeTo(null);
                f1_x = frame.getX();
                f1_y = frame.getY();
                f1_w = frame.getWidth();
            } else {

                frame.setTitle(tryb);
                frame.setLocation((int) f1_x + (int) f1_w, (int) f1_y);
            }
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));
            frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            frame.setVisible(true);

        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public class dodaj_foldery implements Runnable {

        private File[] pliki;
        private boolean podfoldery;

        /**
         * Dodawanie ścieżek folderów do listy przeszukiwanych katalogów
         *
         * Metoda dodaje do listy przeszukiwanych katalogów ścieżki folderów, w
         * których będą szukane duplikaty plików graficznych. W zależności od
         * trybu działa metoda może uwzględniać również ścieżki folderów
         * podrzędnych. Metoda działa w osobnym wątku.
         *
         *
         * @param pliki ścieżka do przeszukiwanego folderu
         * @param podfoldery wartość logiczna pozwalająca na uwzględnienie
         * podfolderów
         */
        public dodaj_foldery(File[] pliki, boolean podfoldery) {
            this.pliki = pliki;
            this.podfoldery = podfoldery;
        }

        public void run() {
            int zw = 0;
            try {
                licznik_global = 0;
                dodawanie_folderow2 = new ArrayList<String>();
                tryb = "Dodawanie folderów";
                jLabel6.setText("Znalezionych folderów: 0");

                if (podfoldery == true) {
                    for (File plik : pliki) {
                        if (zakoncz_watek_zmienna == 1) {
                            break;
                        }
                        String sciezka = plik.getAbsolutePath();
                        if (!model2.contains(sciezka)) {

                            dodawanie_folderow2.add(sciezka);
                        }

                        podfoldery(sciezka);

                    }

                } else {

                    for (File plik : pliki) {
                        if (zakoncz_watek_zmienna == 1) {
                            break;
                        }
                        String sciezka = plik.getAbsolutePath();
                        if (!model2.contains(sciezka)) {
                            licznik_global = licznik_global + 1;
                            jLabel6.setText("Znalezionych folderów: " + licznik_global);

                            dodawanie_folderow2.add(sciezka);
                        }
                    }
                }
                zw = 1;
                zakoncz_watek();
            } catch (Exception nfe) {
                if (zw == 0) {
                    zakoncz_watek();
                }

            }

        }
    }

    public class dodaj_folder_pliki implements Runnable {

        private File[] pliki;
        private boolean podfoldery;

        /**
         * Dodawanie ścieżek plików do listy plików graficznych, dla których
         * będą szukane duplikaty
         *
         * Metoda dodaje do listy plików graficznych ścieżki plików znajdujących
         * się w podanym folderze, dla których będą szukane duplikaty. W
         * zależności od trybu działa metoda może uwzględniać również ścieżki
         * plików w folderach podrzędnych. Metoda działa w osobnym wątku.
         *
         * @param pliki ścieżka do folderu z plikami graficznymi
         * @param podfoldery wartość logiczna pozwalająca na uwzględnienie
         * podfolderów
         */
        public dodaj_folder_pliki(File[] pliki, boolean podfoldery) {
            this.pliki = pliki;
            this.podfoldery = podfoldery;
        }

        @Override
        public void run() {
            int zw = 0;
            try {

                dodawanie_folderow = new ArrayList<String>();
                tryb = "Dodawanie plików z folderów";
                jLabel6.setText("Znalezionych plików: 0");
                int licznik = 0;

                for (File plik : pliki) {
                    if (zakoncz_watek_zmienna == 1) {
                        break;
                    }
                    String sciezka = plik.getAbsolutePath();

                    try {

                        File dir = new File(sciezka);

                        String[] extensions = new String[]{"*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif", "*.jfi", "*.jp2", "*.j2k", "*.jpf", "*.mj2",
                            "*.gif", "*.bmp", "*.dib", "*.png", "*.JPG", "*.JPEG", "*.JPE", "*.JIF", "*.JFIF", "*.JFI", "*.JP2", "*.J2K", "*.JPF", "*.MJ2", "*.TIFF", "*.TIF",
                            "*.GIF", "*.BMP", "*.DIB", "*.PNG"};

                        List<File> files2 = null;

                        if (podfoldery == true) {
                            files2 = (List<File>) FileUtils.listFiles(dir, new WildcardFileFilter(extensions), TrueFileFilter.INSTANCE);
                        } else {
                            files2 = (List<File>) FileUtils.listFiles(dir, new WildcardFileFilter(extensions), null);
                        }

                        for (File file : files2) {
                            if (zakoncz_watek_zmienna == 1) {
                                break;
                            }
                            sciezka = file.getCanonicalPath();

                            if (!model1.contains(sciezka)) {

                                licznik = licznik + 1;
                                jLabel6.setText("Znalezionych plików: " + licznik);

                                dodawanie_folderow.add(sciezka);
                            }

                        }
                    } catch (Exception e) {
                        continue;
                    }

                }
                jList1.setModel(model1);

                zw = 1;
                zakoncz_watek();
            } catch (Exception nfe) {
                if (zw == 0) {
                    zakoncz_watek();
                }
            }

        }
    }

    /**
     * Blokowanie lub odblokowanie przycisków GUI
     *
     * Metoda blokuje lub odblokowuje przyciski interfejsu graficznego w
     * zależności od akcji użytkownika.
     *
     */
    public void przyciski() {

        int wybrany = jTable2.getSelectedRow();

        if (wybrany >= 0) {

            jButton17.setEnabled(true);
            jButton19.setEnabled(true);
        } else {
            jButton17.setEnabled(false);
            jButton19.setEnabled(false);
        }

        int rozmiar_tabeli = jTable2.getRowCount();
        if (rozmiar_tabeli > 0) {
            jButton20.setEnabled(true);

        } else {
            jButton20.setEnabled(false);
        }

    }

    /**
     * Dodawanie ścieżek podfolderów do listy przeszukiwanych katalogów
     *
     * Metoda dodaje do listy przeszukiwanych katalogów ścieżki podfolderów, w
     * których będą szukane duplikaty plików graficznych..
     *
     * @param sciezka ścieżka do folderu nadrzędnego
     */
    public void podfoldery(String sciezka) {

        String sciezka2;

        File dir = new File(sciezka);

        File[] directories = dir.listFiles(File::isDirectory);

        for (File file : directories) {
            if (zakoncz_watek_zmienna == 1) {
                break;
            }

            try {
                sciezka2 = file.getCanonicalPath();

                if (!model2.contains(sciezka2)) {
                    dodawanie_folderow2.add(sciezka2);
                    licznik_global = licznik_global + 1;
                    jLabel6.setText("Znalezionych folderów: " + licznik_global);
                }

                podfoldery(sciezka2);

            } catch (Exception e) {

                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                continue;
            }

        }

    }

    /**
     * Konwersja obrazu do formatu 8-bitowego RGB
     *
     * Metoda konwertuje obraz pobrany z pliku źródłowego do obrazu 8-bitowego z
     * kolorami RGB.
     *
     * @param sciezka ścieżka do pliku graficznego
     * @return newBufferedImage 8-bitowy obraz RGB
     */
    public BufferedImage konwersja_obrazu(String sciezka) {

        try {

            BufferedImage do_skonwertowania = ImageIO.read(new File(sciezka));

            BufferedImage newBufferedImage = new BufferedImage(do_skonwertowania.getWidth(),
                    do_skonwertowania.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            newBufferedImage.createGraphics().drawImage(do_skonwertowania, 0, 0, Color.WHITE, null);
            return newBufferedImage;

        } catch (Exception e) {

            return null;

        }

    }

    /**
     * Sprawdzenie rozdzielczości pliku graficznego
     *
     * Metoda sprawdza rozdzielczość pliku graficznego na dysku twardym.
     *
     * @param sciezka ścieżka do pliku graficznego
     * @return rozdzielczosc rozdzielczość obrazu.
     */
    public String sprawdzRozdzielczosc(String sciezka) throws IOException {
        BufferedImage img1 = ImageIO.read(new File(sciezka));
        int szerokosc = img1.getWidth();
        int wysokosc = img1.getHeight();
        String rozdzielczosc = szerokosc + "x" + wysokosc;

        return rozdzielczosc;
    }

    /**
     * Porównywanie obrazów metodą jpHash
     *
     * Metoda porównuje dwa obrazy metodą jpHash. Najpierw sprawdzane jest czy
     * hashe plików są już zapisane w tablicy hashów, w przeciwnym razie
     * sprawdzana jest zawartość bazy danych z informacjami o plikach
     * graficznych.
     *
     * Jeżeli w bazie danych brakuje informacji o pliku, obliczany jest nowy
     * hash, rozdzielczość pliku oraz wartość sumy kontrolnej, a następnie
     * informacje zapisywane są do bazy danych i tablic pomocniczych. W razie
     * konieczności obrazy konwertowane są do 8-bitowego obrazu RGB, które
     * obsługuje metoda porównywania.
     *
     * Jeżeli w bazie istnieją już informacje o pliku, suma kontrolna pliku
     * pobrana z bazy porównywana jest z nowo obliczoną sumą pliku.
     *
     * Jeżeli wartości różnią się, informacje o pliku zostają obliczone na nowo
     * i zapisane do bazy danych, w przeciwnym razie informacje o pliku
     * pobierane są z bazy danych.
     *
     * @param oryginal ścieżka do pierwszego z porównywanych plików
     * @param porownywany ścieżka do drugiego z porównywanych plików
     * @return hash_procent procent podobieństwa plików graficznych
     */
    public String jpHash_m(String oryginal, String porownywany) {

        RadialHash jp = new RadialHash(1);

        try {

            RadialHash hash1, hash2;
            if (jpHash_hashe.containsKey(oryginal)) {
                hash1 = jpHash_hashe.get(oryginal);
            } else {

                Object[] wynik = new Object[6];
                wynik = sql.sprawdzBaze(oryginal);
                if (wynik[0] == null) {

                    File input = new File(oryginal);
                    String typ = new MimetypesFileTypeMap().getContentType(input);
                    if (typ.equals("image/jpeg")) {
                        hash1 = jpHash.getImageRadialHash(oryginal);
                    } else {
                        BufferedImage skonwertowany = konwersja_obrazu(oryginal);
                        hash1 = jpHash.getImageRadialHash(skonwertowany);
                    }
                    jpHash_hashe.put(oryginal, hash1);
                    String rozdzielczosc = "";
                    if (rozdzielczosci.containsKey(oryginal)) {
                        rozdzielczosc = rozdzielczosci.get(oryginal);
                    } else {
                        rozdzielczosc = sprawdzRozdzielczosc(oryginal);
                        rozdzielczosci.put(oryginal, rozdzielczosc);
                    }

                    long crc = sprawdzCRC(oryginal);

                    sql.wstawWiersz(oryginal, hash1.toString(), "null", String.valueOf(crc), rozdzielczosc);

                } else {
                    long crc_pliku = sprawdzCRC(oryginal);
                    long crc_z_bazy = Long.valueOf(wynik[4].toString());

                    if (crc_pliku == crc_z_bazy) {
                        RadialHash a = new RadialHash(1);

                        String hash2_string = wynik[2].toString();
                        if (hash2_string.equals("null")) {
                            File input = new File(oryginal);
                            String typ = new MimetypesFileTypeMap().getContentType(input);
                            if (typ.equals("image/jpeg")) {

                                hash1 = jpHash.getImageRadialHash(oryginal);
                            } else {
                                BufferedImage skonwertowany = konwersja_obrazu(oryginal);
                                hash1 = jpHash.getImageRadialHash(skonwertowany);
                            }
                            sql.aktualizujWiersz(oryginal, "jphash_hash", hash1.toString());
                        } else {
                            hash1 = a.fromString(hash2_string);
                        }

                        jpHash_hashe.put(oryginal, hash1);
                        String rozdzielczosc = wynik[5].toString();

                        if (!rozdzielczosci.containsKey(oryginal)) {
                            rozdzielczosci.put(oryginal, rozdzielczosc);
                        }

                    } else {

                        File input = new File(oryginal);
                        String typ = new MimetypesFileTypeMap().getContentType(input);
                        if (typ.equals("image/jpeg")) {

                            hash1 = jpHash.getImageRadialHash(oryginal);
                        } else {
                            BufferedImage skonwertowany = konwersja_obrazu(oryginal);
                            hash1 = jpHash.getImageRadialHash(skonwertowany);
                        }
                        jpHash_hashe.put(oryginal, hash1);
                        long crc = sprawdzCRC(oryginal);

                        String rozdzielczosc = "";
                        if (rozdzielczosci.containsKey(oryginal)) {
                            rozdzielczosc = sprawdzRozdzielczosc(oryginal);
                        } else {
                            rozdzielczosc = sprawdzRozdzielczosc(oryginal);
                            rozdzielczosci.put(oryginal, rozdzielczosc);
                        }

                        sql.aktualizujWiersz(oryginal, "jphash_hash", hash1.toString());
                        sql.aktualizujWiersz(oryginal, "hash_pliku", String.valueOf(crc));
                        sql.aktualizujWiersz(oryginal, "rozdzielczosc", rozdzielczosc);
                    }

                }

            }
//zmiana

            if (jpHash_hashe.containsKey(porownywany)) {
                hash2 = jpHash_hashe.get(porownywany);
            } else {

                Object[] wynik = new Object[6];
                wynik = sql.sprawdzBaze(porownywany);
                if (wynik[0] == null) {

                    File input = new File(porownywany);
                    String typ = new MimetypesFileTypeMap().getContentType(input);
                    if (typ.equals("image/jpeg")) {
                        hash2 = jpHash.getImageRadialHash(porownywany);
                    } else {
                        BufferedImage skonwertowany = konwersja_obrazu(porownywany);
                        hash2 = jpHash.getImageRadialHash(skonwertowany);
                    }
                    jpHash_hashe.put(porownywany, hash2);
                    String rozdzielczosc = "";
                    if (rozdzielczosci.containsKey(porownywany)) {
                        rozdzielczosc = rozdzielczosci.get(porownywany);
                    } else {
                        rozdzielczosc = sprawdzRozdzielczosc(porownywany);
                        rozdzielczosci.put(porownywany, rozdzielczosc);
                    }
                    long crc = sprawdzCRC(porownywany);
                    sql.wstawWiersz(porownywany, hash2.toString(), "null", String.valueOf(crc), rozdzielczosc);

                } else {
                    long crc_pliku = sprawdzCRC(porownywany);
                    long crc_z_bazy = Long.valueOf(wynik[4].toString());

                    if (crc_pliku == crc_z_bazy) {
                        RadialHash a = new RadialHash(1);

                        String hash2_string = wynik[2].toString();
                        if (hash2_string.equals("null")) {
                            File input = new File(porownywany);
                            String typ = new MimetypesFileTypeMap().getContentType(input);
                            if (typ.equals("image/jpeg")) {

                                hash2 = jpHash.getImageRadialHash(porownywany);
                            } else {
                                BufferedImage skonwertowany = konwersja_obrazu(porownywany);
                                hash2 = jpHash.getImageRadialHash(skonwertowany);
                            }
                            sql.aktualizujWiersz(porownywany, "jphash_hash", hash2.toString());
                        } else {
                            hash2 = a.fromString(hash2_string);
                        }

                        jpHash_hashe.put(porownywany, hash2);
                        String rozdzielczosc = wynik[5].toString();

                        if (!rozdzielczosci.containsKey(porownywany)) {
                            rozdzielczosci.put(porownywany, rozdzielczosc);
                        }
                    } else {

                        File input = new File(porownywany);
                        String typ = new MimetypesFileTypeMap().getContentType(input);
                        if (typ.equals("image/jpeg")) {

                            hash2 = jpHash.getImageRadialHash(porownywany);
                        } else {
                            BufferedImage skonwertowany = konwersja_obrazu(porownywany);
                            hash2 = jpHash.getImageRadialHash(skonwertowany);
                        }
                        jpHash_hashe.put(porownywany, hash2);
                        long crc = sprawdzCRC(porownywany);
                        String rozdzielczosc = "";
                        if (rozdzielczosci.containsKey(porownywany)) {
                            rozdzielczosc = sprawdzRozdzielczosc(porownywany);
                        } else {
                            rozdzielczosc = sprawdzRozdzielczosc(porownywany);
                            rozdzielczosci.put(porownywany, rozdzielczosc);
                        }
                        sql.aktualizujWiersz(porownywany, "jphash_hash", hash2.toString());
                        sql.aktualizujWiersz(porownywany, "hash_pliku", String.valueOf(crc));
                        sql.aktualizujWiersz(porownywany, "rozdzielczosc", rozdzielczosc);
                    }

                }

            }
//zmiana
            double wynik = jpHash.getSimilarity(hash1, hash2);

            String hash_procent = (String.format("%.2f", wynik * 100));

            return hash_procent;

        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return "Błąd";
        }

    }

    /**
     * Porównywanie obrazów metodą radialHash
     *
     * Metoda porównuje dwa obrazy metodą radialHash. Najpierw sprawdzane jest
     * czy hashe plików są już zapisane w tablicy hashów, w przeciwnym razie
     * sprawdzana jest zawartość bazy danych z informacjami o plikach
     * graficznych.
     *
     * Jeżeli w bazie danych brakuje informacji o pliku, obliczany jest nowy
     * hash, rozdzielczość pliku oraz wartość sumy kontrolnej, a następnie
     * informacje zapisywane są do bazy danych i tablic pomocniczych.
     *
     * Jeżeli w bazie istnieją już informacje o pliku, suma kontrolna pliku
     * pobrana z bazy porównywana jest z nowo obliczoną sumą pliku.
     *
     * Jeżeli wartości różnią się, informacje o pliku zostają obliczone na nowo
     * i zapisane do bazy danych, w przeciwnym razie informacje o pliku
     * pobierane są z bazy danych.
     *
     * @param oryginal ścieżka do pierwszego z porównywanych plików
     * @param porownywany ścieżka do drugiego z porównywanych plików
     * @return wynik_zwroc procent podobieństwa plików graficznych
     */
    public String imagePHash_m(String oryginal, String porownywany) {

        try {

            ImagePHash imagePHash = new ImagePHash();

            String hash1, hash2;

            if (imagePHash_hashe.containsKey(oryginal)) {
                hash1 = imagePHash_hashe.get(oryginal);
            } else {

                Object[] wynik = new Object[6];
                wynik = sql.sprawdzBaze(oryginal);
                if (wynik[0] == null) {

                    hash1 = imagePHash.getHash(new FileInputStream(oryginal));

                    imagePHash_hashe.put(oryginal, hash1);
                    String rozdzielczosc = "";
                    if (rozdzielczosci.containsKey(oryginal)) {
                        rozdzielczosc = rozdzielczosci.get(oryginal);
                    } else {
                        rozdzielczosc = sprawdzRozdzielczosc(oryginal);
                        rozdzielczosci.put(oryginal, rozdzielczosc);
                    }

                    long crc = sprawdzCRC(oryginal);
                    sql.wstawWiersz(oryginal, "null", hash1, String.valueOf(crc), rozdzielczosc);

                } else {
                    long crc_pliku = sprawdzCRC(oryginal);
                    long crc_z_bazy = Long.valueOf(wynik[4].toString());

                    if (crc_pliku == crc_z_bazy) {
                        RadialHash a = new RadialHash(1);

                        hash1 = wynik[3].toString();

                        if (hash1.equals("null")) {

                            hash1 = imagePHash.getHash(new FileInputStream(oryginal));

                            sql.aktualizujWiersz(oryginal, "phash_hash", hash1);
                        }

                        imagePHash_hashe.put(oryginal, hash1);
                        String rozdzielczosc = wynik[5].toString();

                        if (!rozdzielczosci.containsKey(oryginal)) {
                            rozdzielczosci.put(oryginal, rozdzielczosc);
                        }

                    } else {

                        hash1 = imagePHash.getHash(new FileInputStream(oryginal));

                        imagePHash_hashe.put(oryginal, hash1);
                        long crc = sprawdzCRC(oryginal);

                        String rozdzielczosc = "";
                        if (rozdzielczosci.containsKey(oryginal)) {
                            rozdzielczosc = sprawdzRozdzielczosc(oryginal);
                        } else {
                            rozdzielczosc = sprawdzRozdzielczosc(oryginal);
                            rozdzielczosci.put(oryginal, rozdzielczosc);
                        }

                        sql.aktualizujWiersz(oryginal, "phash_hash", hash1);
                        sql.aktualizujWiersz(oryginal, "hash_pliku", String.valueOf(crc));
                        sql.aktualizujWiersz(oryginal, "rozdzielczosc", rozdzielczosc);
                    }

                }

            }
//zmiana

            if (imagePHash_hashe.containsKey(porownywany)) {
                hash2 = imagePHash_hashe.get(porownywany);
            } else {

                Object[] wynik = new Object[6];
                wynik = sql.sprawdzBaze(porownywany);
                if (wynik[0] == null) {

                    hash2 = imagePHash.getHash(new FileInputStream(porownywany));

                    imagePHash_hashe.put(porownywany, hash2);
                    String rozdzielczosc = "";
                    if (rozdzielczosci.containsKey(porownywany)) {
                        rozdzielczosc = rozdzielczosci.get(porownywany);
                    } else {
                        rozdzielczosc = sprawdzRozdzielczosc(porownywany);
                        rozdzielczosci.put(porownywany, rozdzielczosc);
                    }

                    long crc = sprawdzCRC(porownywany);
                    sql.wstawWiersz(porownywany, "null", hash2, String.valueOf(crc), rozdzielczosc);

                } else {
                    long crc_pliku = sprawdzCRC(porownywany);
                    long crc_z_bazy = Long.valueOf(wynik[4].toString());

                    if (crc_pliku == crc_z_bazy) {
                        RadialHash a = new RadialHash(1);

                        hash2 = wynik[3].toString();
                        if (hash2.equals("null")) {

                            hash2 = imagePHash.getHash(new FileInputStream(porownywany));

                            sql.aktualizujWiersz(porownywany, "phash_hash", hash2);
                        }

                        imagePHash_hashe.put(porownywany, hash2);
                        String rozdzielczosc = wynik[5].toString();

                        if (!rozdzielczosci.containsKey(porownywany)) {
                            rozdzielczosci.put(porownywany, rozdzielczosc);
                        }

                    } else {

                        hash2 = imagePHash.getHash(new FileInputStream(porownywany));

                        imagePHash_hashe.put(porownywany, hash2);
                        long crc = sprawdzCRC(porownywany);

                        String rozdzielczosc = "";
                        if (rozdzielczosci.containsKey(porownywany)) {
                            rozdzielczosc = sprawdzRozdzielczosc(porownywany);
                        } else {
                            rozdzielczosc = sprawdzRozdzielczosc(porownywany);
                            rozdzielczosci.put(porownywany, rozdzielczosc);
                        }

                        sql.aktualizujWiersz(porownywany, "phash_hash", hash2);
                        sql.aktualizujWiersz(porownywany, "hash_pliku", String.valueOf(crc));
                        sql.aktualizujWiersz(porownywany, "rozdzielczosc", rozdzielczosc);
                    }

                }

            }
//zmiana

            // long hash_1_dec = Long.parseLong(hash_1, 2);
            // long hash_2_dec = Long.parseLong(hash_2, 2);
            int distance = imagePHash.distance(hash1, hash2);

            int roznica = 49 - distance;
            if (roznica == 0) {
                roznica = 1;
            }
            int a = 49 - roznica;
            int b = 49 + roznica;
            float c = b / 2;
            float d = a / c;
            float e = d * 100;
            if (e > 100) {
                e = 100;
            }
            float procent = 100 - e;

            String wynik_zwroc = Float.toString(procent);
            return wynik_zwroc;

        } catch (Exception ex) {

            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            String wynik_zwroc = "Błąd";

            return wynik_zwroc;
        }

    }

    /**
     * Porównywanie obrazów metodą imgDiff
     *
     * Metoda porównuje dwa obrazy metodą imgDiff. Najpierw sprawdzane jest czy
     * hashe plików są już zapisane w tablicy wyników, w przeciwnym razie
     * obliczany jest nowy stopień podobieństwa plików oraz ich rozdzielczość, a
     * następnie informacje zapisywane są do tablic pomocniczych.
     *
     * @param oryginal ścieżka do pierwszego z porównywanych plików
     * @param porownywany ścieżka do drugiego z porównywanych plików
     * @return wynik2 procent podobieństwa plików graficznych
     */
    public String imgDiff_m(String oryginal, String porownywany) {

        try {
            String wynik;

            if (imgDiff_wyniki.containsKey(oryginal + porownywany)) {

                wynik = imgDiff_wyniki.get(oryginal + porownywany);

            } else {

                imgDiff imageDiff = new imgDiff();

                wynik = imageDiff.getPercentage(oryginal, porownywany);

                imgDiff_wyniki.put(oryginal + porownywany, wynik);

                if (!rozdzielczosci.containsKey(oryginal)) {
                    String rozdzielczosc = sprawdzRozdzielczosc(oryginal);
                    rozdzielczosci.put(oryginal, rozdzielczosc);
                }

                if (!rozdzielczosci.containsKey(porownywany)) {
                    String rozdzielczosc = sprawdzRozdzielczosc(porownywany);
                    rozdzielczosci.put(porownywany, rozdzielczosc);
                }

            }

            wynik = wynik.replace(",", ".");
            double procent = Double.parseDouble(wynik);
            if (procent >= 0 && procent <= 100) {
                String wynik2 = String.format("%.2f", procent);
                return wynik2;
            } else {
                return "Błąd";
            }

        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return "Błąd";
        }

    }

    /**
     * Metoda inicjalizująca aplikację
     *
     * Metoda sprawdza czy w folderze aplikacji znajdują się wszystkie wymagane
     * biblioteki i inicjalizuje wszystkie komponenty aplikacji.
     *
     */
    public Main() {

        File a = new File("lib\\commons-io-2.5.jar");
        File b = new File("lib\\commons-cli-1.4.jar");
        File c = new File("lib\\jpHash.jar");
        File d = new File("lib\\imgDiff2.jar");
        File e = new File("lib\\sqlite-jdbc-3.21.0.jar");
        File f = new File("lib\\ImagePHash.jar");

        if (a.exists() && !a.isDirectory() && b.exists() && !b.isDirectory() && c.exists() && !c.isDirectory() && d.exists() && !d.isDirectory() && e.exists() && !e.isDirectory() && f.exists() && !f.isDirectory()) {

        } else {
            komunikat("Nie odnaleziono wymaganych bibliotek");
            System.exit(0);
        }

        initComponents();
        model_tabeli = ((DefaultTableModel) jTable2.getModel());
        jList1.setModel(model1);
        jList2.setModel(model2);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jButton3 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jButton11 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jCheckBox4 = new javax.swing.JCheckBox();

        jDialog1.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jDialog1.setTitle("Proszę czekać");
        jDialog1.setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        jDialog1.setMinimumSize(new java.awt.Dimension(197, 153));
        jDialog1.setResizable(false);
        jDialog1.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                jDialog1WindowClosed(evt);
            }
        });

        jButton3.setText("Anuluj");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel6.setText("Znalezionych duplikatów: 0");

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Duplikwidator");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));
        setMaximumSize(new java.awt.Dimension(1176, 653));
        setMinimumSize(new java.awt.Dimension(1176, 653));
        setPreferredSize(new java.awt.Dimension(1176, 653));
        setResizable(false);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton1.setText("Znajdź duplikaty");
        jButton1.setEnabled(false);
        jButton1.setMaximumSize(new java.awt.Dimension(111, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(111, 23));
        jButton1.setPreferredSize(new java.awt.Dimension(111, 23));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jList1.setBackground(new java.awt.Color(248, 248, 248));
        jList1.setAutoscrolls(false);
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jList1);

        jButton11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton11.setText("Usuń z listy");
        jButton11.setEnabled(false);
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton10.setText("Usuń z listy");
        jButton10.setEnabled(false);
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("Znajdź duplikaty plików graficznych: ");

        jButton12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton12.setText("Dodaj plik");
        jButton12.setMaximumSize(new java.awt.Dimension(133, 23));
        jButton12.setMinimumSize(new java.awt.Dimension(133, 23));
        jButton12.setPreferredSize(new java.awt.Dimension(133, 23));
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jLabel2.setText("Metoda porównywania plików:");

        jCheckBox1.setText("pHash");
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jCheckBox3.setText("radialHash");
        jCheckBox3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox3ItemStateChanged(evt);
            }
        });

        jLabel3.setText("Minimalny procent podobieństwa:");

        jSlider1.setMaximum(10000);
        jSlider1.setValue(9500);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("Wskaż foldery do przeszukania:");

        jList2.setBackground(new java.awt.Color(248, 248, 248));
        jList2.setAutoscrolls(false);
        jList2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList2ValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jList2);

        jButton13.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton13.setText("Dodaj ścieżkę");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jButton14.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton14.setLabel("Dodaj ścieżkę");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText("95.00");
        jTextField1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField1CaretUpdate(evt);
            }
        });
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton15.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton15.setText("Wyczyść listę");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        jButton16.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton16.setText("Wyczyść listę");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });

        jTable2.setAutoCreateRowSorter(true);
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Pierwszy plik", "Drugi plik", "Wymiary obrazów", "Podobieństwo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable2.getTableHeader().setReorderingAllowed(false);
        jTable2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTable2FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTable2FocusLost(evt);
            }
        });
        jScrollPane1.setViewportView(jTable2);
        if (jTable2.getColumnModel().getColumnCount() > 0) {
            jTable2.getColumnModel().getColumn(2).setPreferredWidth(200);
            jTable2.getColumnModel().getColumn(2).setMaxWidth(200);
            jTable2.getColumnModel().getColumn(3).setPreferredWidth(100);
            jTable2.getColumnModel().getColumn(3).setMaxWidth(100);
        }

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Wyniki wyszukiwania:");

        jButton17.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton17.setText("Odrzuć wynik");
        jButton17.setEnabled(false);
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });

        jButton18.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton18.setText("Wyczyść listę");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        jButton19.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton19.setText("Podgląd plików");
        jButton19.setEnabled(false);
        jButton19.setMaximumSize(new java.awt.Dimension(133, 48));
        jButton19.setMinimumSize(new java.awt.Dimension(133, 48));
        jButton19.setPreferredSize(new java.awt.Dimension(133, 48));
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });

        jButton20.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton20.setText("Usuń duplikaty");
        jButton20.setEnabled(false);
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
            }
        });

        jCheckBox4.setText("imgDiff");
        jCheckBox4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox4ItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel1)
                        .addGap(362, 362, 362)
                        .addComponent(jLabel4))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel3)
                        .addGap(6, 6, 6)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(190, 190, 190)
                        .addComponent(jLabel2)
                        .addGap(10, 10, 10)
                        .addComponent(jCheckBox1)
                        .addGap(2, 2, 2)
                        .addComponent(jCheckBox3)
                        .addGap(2, 2, 2)
                        .addComponent(jCheckBox4))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel5))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 987, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(40, 40, 40)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton16, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel3))
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2)
                    .addComponent(jCheckBox1)
                    .addComponent(jCheckBox3)
                    .addComponent(jCheckBox4))
                .addGap(46, 46, 46)
                .addComponent(jLabel5)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(44, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Wyszukiwanie duplikatów plików graficznych
     *
     * Metoda wyszukuje duplikaty dla każdego pliku z listy plików graficznych
     * dla których szukamy duplikatów wśród wszystkich plików znajdujących się w
     * folderach z listy przeszukiwanych katalogów.
     *
     * W pierwszej kolejności metoda sprawdza czy pliki nie zostały już
     * wcześniej porównane, a wynik porównywania zapisany w tablicy pomocniczej
     * - w takim przypadku od razu zwracany jest wynik z tablicy.
     *
     * Wynik dodawany jest do listy duplikatów jeżeli podobieństwo obrazów
     * według wybranej metody porównywania plików jest większe niż minimalne
     * podobieństwo określone przez użtytkownika. Metoda działa w osobnym wątku.
     *
     */
    public class wyszukaj_duplikaty implements Runnable {

        private int var;

        public wyszukaj_duplikaty(int var) {
            this.var = var;
        }

        public void run() {
            int zw = 0;
            tryb = "Dodawanie duplikatów";
            jLabel6.setText("Znalezionych duplikatów: 0");
            int licznik = 0;

            try {

                dodawanie_duplikatow = new ArrayList<String[]>();
                ab_ba = new ArrayList<String>();

                boolean imagephash_cb = jCheckBox1.isSelected();
                boolean jphash_cb = jCheckBox3.isSelected();
                boolean imgdiff_cb = jCheckBox4.isSelected();

                if (jphash_cb == false && imagephash_cb == false && imgdiff_cb == false) {
                    blad("Nie wybrano metody porównywania plików");
                    zakoncz_watek();
                } else {

                    List<String> pliki_do_sprawdzenia = new ArrayList<String>();
                    List<String> pliki_duplikaty = new ArrayList<String>();

                    int ilosc_elementow_sciezki = model2.getSize();
                    int ilosc_elementow_pliki = model1.getSize();
                    //komentarz

                    duplikaty_elementy.clear();
                    elementy.clear();

                    String[] extensions = new String[]{"*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif", "*.jfi", "*.jp2", "*.j2k", "*.jpf", "*.mj2",
                        "*.gif", "*.bmp", "*.dib", "*.png", "*.JPG", "*.JPEG", "*.JPE", "*.JIF", "*.JFIF", "*.JFI", "*.JP2", "*.J2K", "*.JPF", "*.MJ2", "*.TIFF", "*.TIF",
                        "*.GIF", "*.BMP", "*.DIB", "*.PNG"};

                    try {

                        for (int i = 0; i < ilosc_elementow_sciezki; i++) {

                            if (zakoncz_watek_zmienna == 1) {
                                break;
                            }

                            String sciezka = model2.elementAt(i);
                            File dir = new File(sciezka);

                            try {

                                List<File> files2 = (List<File>) FileUtils.listFiles(dir, new WildcardFileFilter(extensions), null);

                                for (File file : files2) {

                                    if (zakoncz_watek_zmienna == 1) {
                                        break;
                                    }

                                    String sciezka_do_pliku = file.getCanonicalPath();

                                    if (!pliki_duplikaty.contains(sciezka_do_pliku)) {
                                        pliki_duplikaty.add(sciezka_do_pliku);
                                    }

                                }
                            } catch (Exception e) {
                                continue;
                            }

                        }

                        for (int i = 0; i < ilosc_elementow_pliki; i++) {

                            if (zakoncz_watek_zmienna == 1) {
                                break;
                            }

                            for (String plik_porownywany : pliki_duplikaty) {

                                if (zakoncz_watek_zmienna == 1) {
                                    break;
                                }

                                if (model1.elementAt(i).equals(plik_porownywany)) {
                                    continue;
                                }
                                if (ab_ba.contains(plik_porownywany + model1.elementAt(i))) {
                                    continue;
                                } else {
                                    ab_ba.add(model1.elementAt(i) + plik_porownywany);
                                }

                                try {

                                    int dzielnik = 0;
                                    double wynikx = 0;

                                    if (jphash_cb == true) {

                                        String wynik_1 = jpHash_m(model1.elementAt(i), plik_porownywany);

                                        wynik_1 = wynik_1.replace(",", ".");
                                        double wynik_1_procent;

                                        if (wynik_1.equals("Błąd")) {
                                        } else {
                                            wynikx = wynikx + Double.parseDouble(wynik_1);
                                            dzielnik = dzielnik + 1;
                                        }
                                    }

                                    if (imagephash_cb == true) {
                                        String hash_string = imagePHash_m(model1.elementAt(i), plik_porownywany);
                                        String wynik_2 = hash_string.replace(",", ".");
                                        if (wynik_2.equals("Błąd")) {

                                        } else {
                                            wynikx = wynikx + Double.parseDouble(wynik_2);
                                            dzielnik = dzielnik + 1;
                                        }

                                    }

                                    if (imgdiff_cb == true) {
                                        String wynik_3 = imgDiff_m(model1.elementAt(i), plik_porownywany);
                                        wynik_3 = wynik_3.replace(",", ".");
                                        if (wynik_3.equals("Błąd")) {

                                        } else {
                                            wynikx = wynikx + Double.parseDouble(wynik_3);
                                            dzielnik = dzielnik + 1;
                                        }

                                    }

                                    if (dzielnik == 0) {
                                        continue;
                                    }

                                    double wynik_procent = (wynikx / dzielnik);
                                    String wynik = String.format("%.2f", wynik_procent);

                                    if (wynik_procent < stopien_procent) {
                                        continue;
                                    }

                                    String rozdzielczosc_wynik = rozdzielczosci.get(model1.elementAt(i)) + " px vs " + rozdzielczosci.get(plik_porownywany) + " px";

                                    //  duplika
                                    if (duplikaty_elementy.containsKey(model1.elementAt(i))) {
                                        elementy.add(plik_porownywany);
                                        String[] array = elementy.toArray(new String[0]);
                                        duplikaty_elementy.put(model1.elementAt(i), array);

                                    } else {

                                        elementy.clear();
                                        elementy.add(plik_porownywany);
                                        String[] array = elementy.toArray(new String[0]);
                                        duplikaty_elementy.put(model1.elementAt(i), array);

                                    }

                                    licznik = licznik + 1;
                                    jLabel6.setText("Znalezionych duplikatów: " + licznik);

                                    String[] x = new String[4];
                                    x[0] = model1.elementAt(i);
                                    x[1] = plik_porownywany;
                                    x[2] = rozdzielczosc_wynik;
                                    x[3] = wynik;
                                    dodawanie_duplikatow.add(x);

                                } catch (Exception e) {

                                    continue;
                                }

                            }

                        }

                        if (polaczenieDB != null) {
                            if (polaczenieDB.isClosed() != true) {
                                polaczenieDB.close();
                            }
                        }
                        zw = 1;
                        zakoncz_watek();

                        // System.out.println(wyniki_wyszukiwania.get(1)[0]);
                    } catch (Exception e) {
                        //  System.out.println(e.toString());
                        if (zw == 0) {
                            zakoncz_watek();
                        }
                        //     System.out.println("Wyjatek");
                    }

                }

            } catch (Exception nfe) {
                if (zw == 0) {
                    zakoncz_watek();
                }
            }

        }
    }

    /**
     * Odblokowanie / zablokowanie przycisku usuwania elementu z listy
     *
     * Metoda sprawdza czy użytkownik zaznaczył wartość na liście plików
     * graficznych - w przeciwnym razie wyłącza przycisk usuwania elementu z
     * listy.
     *
     */
    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged

        if (!evt.getValueIsAdjusting()) {

            if (jList1.isSelectionEmpty()) {
                jButton10.setEnabled(false);

            } else {
                jButton10.setEnabled(true);
            }

        }


    }//GEN-LAST:event_jList1ValueChanged

    /**
     * Usuwanie zaznaczonych elementów z listy plików graficznych
     *
     * Metoda usuwa elementy wybrane przez użytkownika z listy plików
     * graficznych.
     *
     */
    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed

        List<String> selectedValues = jList1.getSelectedValuesList();

        for (String element : selectedValues) {
            model1.removeElement(element);

        }
        poprawnosc();

    }//GEN-LAST:event_jButton10ActionPerformed

    /**
     * Dodawanie elementów do listy plików graficznych
     *
     * Metoda pokazuje okno dialogowe umożliwiające wybór plików graficznych dla
     * których będą szukane duplikaty i dodaje je do listy.
     *
     */
    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Plik graficzny", "jpg", "jpeg", "jpe", "jif", "jfif", "jfi", "jp2", "j2k", "jpf", "mj2",
                "gif", "bmp", "dib", "png");
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle("Wybierz plik graficzny");
        fc.setMultiSelectionEnabled(true);

        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = fc.getSelectedFiles();

            for (File plik : files) {
                String sciezka = plik.getAbsolutePath();

                if (!model1.contains(sciezka)) {
                    model1.addElement(sciezka);
                }

            }
            poprawnosc();
        } else {

        }


    }//GEN-LAST:event_jButton12ActionPerformed
    /**
     * Odblokowanie / zablokowanie przycisku usuwania elementu z listy
     *
     * Metoda sprawdza czy użytkownik zaznaczył wartość na liście
     * przeszukiwanych folderów - w przeciwnym razie wyłącza przycisk usuwania
     * elementu z listy.
     *
     */
    private void jList2ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList2ValueChanged

        if (!evt.getValueIsAdjusting()) {
            if (jList2.isSelectionEmpty()) {
                jButton11.setEnabled(false);

            } else {
                jButton11.setEnabled(true);
            }

        }


    }//GEN-LAST:event_jList2ValueChanged

    /**
     * Dodawanie elementów do listy przeszukiwanych folderów
     *
     * Metoda pokazuje okno dialogowe umożliwiające wybór przeszukiwanych
     * folderów z opcjonalnym uwzględnieniem podfolderów i dodaje je do listy.
     *
     */
    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        final JFileChooser fc = new JFileChooser();

        fc.setAcceptAllFileFilterUsed(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle("Wybierz folder z plikami graficznymi");
        fc.setMultiSelectionEnabled(true);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // fc.setCurrentDirectory(new File(working_dir));

//In response to a button click:
        int returnVal = fc.showOpenDialog(this);
        boolean podkatalogi;
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            JFrame frame = new JFrame();
            Object[] options = {"Tak",
                "Nie"};
            int wybor = JOptionPane.showOptionDialog(frame,
                    "Uwzględnić zawartość podfolderów?",
                    "Dodawanie plików z folderu",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, //do not use a custom Icon
                    options, //the titles of buttons
                    options[0]); //default button title

            if (wybor == JOptionPane.YES_OPTION) {

                podkatalogi = true;

            } else {

                podkatalogi = false;
            }

            File[] files = fc.getSelectedFiles();

            dodaj_folder_pliki dodaj_folder_pliki = new dodaj_folder_pliki(files, podkatalogi);

            task_1 = new Thread(dodaj_folder_pliki);

            zakoncz_watek_zmienna = 0;
            jDialog1.getRootPane().setBorder(
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

            jDialog1.setLocationRelativeTo(null);
            jDialog1.setVisible(true);

            this.setEnabled(false);
            task_1.start();

        }


    }//GEN-LAST:event_jButton13ActionPerformed

    /**
     * Ustawienie wartości pola tekstowego
     *
     * Metoda ustawia wartość pola tekstowego na podstawie wartości suwaka.
     *
     */
    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged

        double sliderValue = jSlider1.getValue() / 100.00;
        jTextField1.setText(String.format("%.2f", sliderValue));

    }//GEN-LAST:event_jSlider1StateChanged

    /**
     * Ustawienie wartości suwaka
     *
     * Metoda ustawia wartość suwaka na podstawie wartości podanej w polu
     * tekstowym.
     *
     */
    private void jTextField1CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField1CaretUpdate

        try {
            double procent = Double.parseDouble(jTextField1.getText());
            int procent2 = (int) (procent * 100);
            jSlider1.setValue(procent2);
            poprawnosc();
        } catch (Exception e) {
            poprawnosc();

        }

    }//GEN-LAST:event_jTextField1CaretUpdate

    /**
     * Usunięcie elementów z listy plików graficznych
     *
     * Metoda ustawia wszystkie elementy z listy plików graficznych.
     *
     */
    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        model1.removeAllElements();
        poprawnosc();
    }//GEN-LAST:event_jButton15ActionPerformed

    /**
     * Odrzucenie wyniku wyszukiwania duplikatów
     *
     * Metoda odrzuca wybrany wynik z listy znalezionych duplikatów.
     *
     */
    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed

        int[] wybrane_wiersze = jTable2.getSelectedRows();
        int usuniete = 0;

        for (int wiersz : wybrane_wiersze) {

            String nazwa_p1 = model_tabeli.getValueAt(wiersz - usuniete, 0).toString();
            String nazwa_p2 = model_tabeli.getValueAt(wiersz - usuniete, 1).toString();
            String[] elementy2 = duplikaty_elementy.get(nazwa_p1);
            if (Arrays.asList(elementy2).contains(nazwa_p2)) {

                List<String> x = Arrays.asList(elementy2);
                x = new ArrayList<>(x);
                x.remove(nazwa_p2);
                String[] array = x.toArray(new String[0]);
                if (x.isEmpty()) {
                    duplikaty_elementy.remove(nazwa_p1);
                } else {
                    duplikaty_elementy.put(nazwa_p1, array);
                }

            }

            model_tabeli.removeRow(wiersz - usuniete);
            usuniete = usuniete + 1;

        }

        przyciski();
        // String a = jTable2.getValueAt(aa, 0).toString();


    }//GEN-LAST:event_jButton17ActionPerformed

    /**
     * Wyczyszczenie listy wyników wyszukiwania duplikatów
     *
     * Metoda usuwa wszystkie wyniki wyszukiwania duplikatów z listy
     *
     */
    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed

        int rowCount = model_tabeli.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            model_tabeli.removeRow(i);
        }
        duplikaty_elementy.clear();
        elementy.clear();
        przyciski();

    }//GEN-LAST:event_jButton18ActionPerformed

    /**
     * Podgląd duplikatów plików graficznych
     *
     * Metoda umożliwia obejrzenie podglądu plików graficznych uznanych za
     * duplikaty.
     *
     */
    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed

        int wybrany_wiersz = jTable2.getSelectedRow();
        int usuniete = 0;

        String sciezka_plik_1 = jTable2.getValueAt(wybrany_wiersz, 0).toString();
        String sciezka_plik_2 = jTable2.getValueAt(wybrany_wiersz, 1).toString();
        podglad(sciezka_plik_1, "Pierwszy plik");
        podglad(sciezka_plik_2, "Drugi plik");
        przyciski();
    }//GEN-LAST:event_jButton19ActionPerformed
    /**
     * Usunięcie duplikatów plików graficznych z komputera
     *
     * Metoda usuwa znalezione duplikaty plików graficznych, pozostawiając po
     * jednej kopii każdego pliku o najwyższej jakości. Metoda działa w osobnym
     * wątku.
     *
     */
    public class usun_duplikaty implements Runnable {

        private int var;

        public usun_duplikaty(int var) {
            this.var = var;
        }

        public void run() {
            int zw = 0;
            tryb = "Usuwanie duplikatów";
            jLabel6.setText("Usuniętych duplikatów: 0");
            int usuniete = 0;

            try {

                List<String> usuniete_array = new ArrayList<String>();
                usuwanie_duplikatow = new ArrayList<Integer>();

                usuwanie_duplikatow2 = new ArrayList<String>();
                pozostawione = new ArrayList<String>();
                for (Map.Entry<String, String[]> entry : duplikaty_elementy.entrySet()) {

                    String plik1 = entry.getKey();
                    String[] pliki2 = entry.getValue();
                    List<String> x = Arrays.asList(pliki2);
                    x = new ArrayList<>(x);
                    int maxroz = 0;
                    String pozostaw = "";

                    x.add(entry.getKey());
                    for (String plik2 : x) {

                        String rozdzielczosc = rozdzielczosci.get(plik2);
                        String[] rodzieloczosc_split = rozdzielczosc.split("x");
                        int szerokosc = Integer.valueOf(rodzieloczosc_split[0]);
                        int wysokosc = Integer.valueOf(rodzieloczosc_split[1]);
                        int rozdzielczosc_suma = szerokosc + wysokosc;

                        if (rozdzielczosc_suma >= maxroz && !usuwanie_duplikatow2.contains(plik2)) {

                            maxroz = rozdzielczosc_suma;
                            pozostaw = plik2;
                        }
                    }

                    for (String plik2 : x) {
                        if (!plik2.equals(pozostaw)) {
                            if (!usuwanie_duplikatow2.contains(plik2)) {

                                usuwanie_duplikatow2.add(plik2);
                            }

                        }
                    }
                    if (!pozostawione.contains(pozostaw)) {

                        pozostawione.add(pozostaw);
                    }

                }

                for (String sciezka : usuwanie_duplikatow2) {
                    try {

                        Path path = Paths.get(sciezka);
                        Files.delete(path);
                        usuniete = usuniete + 1;
                        jLabel6.setText("Usuniętych duplikatów: " + usuniete);
                    } catch (Exception e) {

                        continue;

                    }

                }

                przyciski();
                zw = 1;
                zakoncz_watek();
            } catch (Exception nfe) {
                //     System.out.println("Wyjatek przy usuwaniu " + nfe);
                if (zw == 0) {
                    przyciski();
                    zakoncz_watek();
                }
            }

        }
    }

    /**
     * Wywołanie funkcji usuwania duplikatów
     *
     * Metoda wywołuje funkcję usuwania duplikatów plików graficznych z
     * możliwością przerwania operacji.
     *
     */
    private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed

        usun_duplikaty usun_duplikaty = new usun_duplikaty(0);

        task_1 = new Thread(usun_duplikaty);

        zakoncz_watek_zmienna = 0;
        jDialog1.getRootPane().setBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

        jDialog1.setLocationRelativeTo(null);
        jDialog1.setVisible(true);

        this.setEnabled(false);
        task_1.start();

    }//GEN-LAST:event_jButton20ActionPerformed

    private void jTable2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable2FocusGained

        przyciski();


    }//GEN-LAST:event_jTable2FocusGained

    private void jTable2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable2FocusLost

        przyciski();
    }//GEN-LAST:event_jTable2FocusLost

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed

    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        zakoncz_watek();

    }//GEN-LAST:event_jButton3ActionPerformed

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained

    }//GEN-LAST:event_formFocusGained

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        jDialog1.setAlwaysOnTop(true);
        jDialog1.setAlwaysOnTop(false);
    }//GEN-LAST:event_formWindowGainedFocus

    private void jDialog1WindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialog1WindowClosed

        if (tryb.equals("Dodawanie plików z folderów")) {

            for (String plik : dodawanie_folderow) {

                model1.addElement(plik);

            }
            poprawnosc();
        }

        if (tryb.equals("Usuwanie duplikatów")) {
            int licznik = 0;

            //  System.out.println(usuwanie_duplikatow2.toString());
            int rowy = model_tabeli.getRowCount();

            for (int licznik_row = 0; licznik_row < rowy; licznik_row++) {

                String p1 = model_tabeli.getValueAt(licznik_row - licznik, 0).toString();
                String p2 = model_tabeli.getValueAt(licznik_row - licznik, 1).toString();

                if (usuwanie_duplikatow2.contains(p2) || usuwanie_duplikatow2.contains(p1)) {

                    model_tabeli.removeRow(licznik_row - licznik);

                    licznik++;
                }

            }
            usuwanie_duplikatow2.clear();
            przyciski();
        }

        if (tryb.equals("Dodawanie folderów")) {

            for (String sciezka : dodawanie_folderow2) {

                model2.addElement(sciezka);

            }
            poprawnosc();
        }

        if (tryb.equals("Dodawanie duplikatów")) {

            for (String[] array : dodawanie_duplikatow) {

                model_tabeli.addRow(new Object[]{array[0], array[1], array[2], array[3] + " %"});

            }

            przyciski();

        }

    }//GEN-LAST:event_jDialog1WindowClosed
    /**
     * Usuwanie zaznaczonych elementów z listy folderów
     *
     * Metoda usuwa elementy wybrane przez użytkownika z listy folderów do
     * przeszukania.
     *
     */
    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        List<String> selectedValues = jList2.getSelectedValuesList();

        for (String element : selectedValues) {
            model2.removeElement(element);

        }
        poprawnosc();
    }//GEN-LAST:event_jButton11ActionPerformed

    /**
     * Wywołanie funkcji wyszukiwania duplikatów
     *
     * Metoda wywołuje funkcję wyszukiwania duplikatów plików graficznych z
     * możliwością przerwania operacji.
     *
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        wyszukaj_duplikaty duplikaty = new wyszukaj_duplikaty(10);
        task_1 = new Thread(duplikaty);

        int rowCount = model_tabeli.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            model_tabeli.removeRow(i);
        }

        przyciski();

        zakoncz_watek_zmienna = 0;

        jDialog1.getRootPane().setBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

        jDialog1.setLocationRelativeTo(null);
        jDialog1.setVisible(true);

        this.setEnabled(false);
        task_1.start();

    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * Dodawanie elementów do listy folderów
     *
     * Metoda pokazuje okno dialogowe umożliwiające wybór folderów w których
     * będą szukane duplikaty i dodaje je do listy.
     *
     */
    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed

        final JFileChooser fc = new JFileChooser();

        fc.setAcceptAllFileFilterUsed(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle("Wskaż folder do przeszukania");
        fc.setMultiSelectionEnabled(true);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // fc.setCurrentDirectory(new File(working_dir));

        //In response to a button click:
        int returnVal = fc.showOpenDialog(this);
        boolean podkatalogi;
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            JFrame frame = new JFrame();
            Object[] options = {"Tak",
                "Nie"};
            int wybor = JOptionPane.showOptionDialog(frame,
                    "Uwzględnić podfoldery?",
                    "Wyszukiwanie duplikatów plików graficznych",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, //do not use a custom Icon
                    options, //the titles of buttons
                    options[0]); //default button title
            File[] files = fc.getSelectedFiles();
            if (wybor == JOptionPane.YES_OPTION) {

                podkatalogi = true;

            } else {
                podkatalogi = false;

            }

            dodaj_foldery dodaj_foldery = new dodaj_foldery(files, podkatalogi);

            task_1 = new Thread(dodaj_foldery);

            zakoncz_watek_zmienna = 0;
            jDialog1.getRootPane().setBorder(
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

            jDialog1.setLocationRelativeTo(null);
            jDialog1.setVisible(true);

            this.setEnabled(false);
            task_1.start();

        }

    }//GEN-LAST:event_jButton14ActionPerformed

    /**
     * Usunięcie elementów z listy folderów
     *
     * Metoda ustawia wszystkie elementy z listy folderów do przeszukania.
     *
     */
    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        model2.removeAllElements();
        poprawnosc();
    }//GEN-LAST:event_jButton16ActionPerformed

    private void jCheckBox4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox4ItemStateChanged
        poprawnosc();
    }//GEN-LAST:event_jCheckBox4ItemStateChanged

    private void jCheckBox3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox3ItemStateChanged
        poprawnosc();
    }//GEN-LAST:event_jCheckBox3ItemStateChanged

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
        poprawnosc();
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed

    }//GEN-LAST:event_jCheckBox1ActionPerformed

    public static void main(String args[]) {

        try {

            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            //   java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            //    java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            //    java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                Main main = new Main();

                if (args.length > 0) {
                    cmd.cmd(args);

                } else {
                    main.setVisible(true);
                }

            }
        });

        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton3;
    public javax.swing.JCheckBox jCheckBox1;
    public javax.swing.JCheckBox jCheckBox3;
    public javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList<String> jList1;
    private javax.swing.JList<String> jList2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    public javax.swing.JSlider jSlider1;
    private javax.swing.JTable jTable2;
    public javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
