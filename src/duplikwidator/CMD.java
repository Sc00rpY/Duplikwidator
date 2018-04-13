package duplikwidator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Klasa odpowiadająca za obsługę aplikacji przy pomocy linii poleceń.
 *
 */
public class CMD extends Main {

    Scanner odczytaj = new Scanner(System.in);
    Options opcje = new Options();

    Option minimalnePodobienstwo = OptionBuilder
            .hasArgs(1)
            .withDescription("ustawia minimalny procent podobieństwa na podaną wartość")
            .create("minimalnePodobienstwo");

    Option szukajW = OptionBuilder
            .hasArgs()
            .withDescription("dodaje folderu (argument p uwzględnia również podfoldery) do listy miejsc, w których będą szukane duplikaty")
            .create("szukajW");

    Option szukajDla = OptionBuilder
            .hasArgs()
            .withDescription("dodaje plik lub zawartość folderu (argument p uwzględnia również podfoldery) do listy plików, dla których szukasz duplikatów")
            .create("szukajDla");

    Option metodyPorownywania = OptionBuilder
            .hasArgs()
            .withDescription("wybiera metody porównywania plików graficznych (pHash, radialHash, imgDiff)")
            .create("metodyPorownywania");

    Option nieSzukajW = OptionBuilder
            .hasArgs()
            .withDescription("usuwa folder (lub wszystkie foldery przy użyciu argumentu w) z listy miejsc, w których będą szukane duplikaty")
            .create("nieSzukajW");

    Option noGUI = OptionBuilder
            .hasArgs(0)
            .withDescription("wyświetlenie komunikatu witającego użytkownika")
            .create("noGUI");

    Option nieSzukajDla = OptionBuilder
            .hasArgs()
            .withDescription("usuwa folder (lub wszystkie foldery przy użyciu argumentu w) z listy miejsc, w których będą szukane duplikaty")
            .create("nieSzukajDla");

    Option pokazMinimalnePodobienstwo = OptionBuilder
            .hasArgs(0)
            .withDescription("wyświetla procent minimalnego podobieństwa plików do uznania ich za duplikaty")
            .create("pokazMinimalnePodobienstwo");

    Option pokazMetodyPorownywania = OptionBuilder
            .hasArgs(0)
            .withDescription("wyświetla metody wybrane do porównywania plików graficznych")
            .create("pokazMetodyPorownywania");

    Option znajdzDuplikaty = OptionBuilder
            .hasArgs(0)
            .withDescription("wyszukuje duplikaty plików graficznych")
            .create("znajdzDuplikaty");

    Option usunDuplikaty = OptionBuilder
            .hasArgs(0)
            .withDescription("usuwa duplikaty plików graficznych")
            .create("usunDuplikaty");

    Option pokazPliki = OptionBuilder
            .hasArgs(0)
            .withDescription("wyświetla listę plików, dla których szukasz duplikatów")
            .create("pokazPliki");

    Option pokazDuplikaty = OptionBuilder
            .hasArgs(0)
            .withDescription("wyświetla listę duplikatów")
            .create("pokazDuplikaty");

    Option odrzucWynik = OptionBuilder
            .hasArgs()
            .withDescription("odrzuca wynik (lub wszystkie wyniki przy użyciu argumentu w) z listy znalezionych duplikatów")
            .create("odrzucWynik");

    Option pokazFoldery = OptionBuilder
            .hasArgs(0)
            .withDescription("wyświetla listę folderów, w których szukasz duplikatów")
            .create("pokazFoldery");

    Option wyjscie = OptionBuilder
            .hasArgs(0)
            .withDescription("kończy pracę programu")
            .create("wyjscie");

    Option pomoc = OptionBuilder
            .hasArgs(0)
            .withDescription("wyświetla dostępne komendy oraz ich opis")
            .create("pomoc");

    CommandLineParser parser = new DefaultParser();

    /**
     * Obsługa aplikacji za pomocą linii poleceń
     *
     * Metoda umożliwia obsługę aplikacji za pomocą komend wprowadzanych poprzez
     * linię poleceń.
     *
     */
    public void cmd(String args[]) {
        tryb_konsoli = 1;

        opcje.addOption(wyjscie);
        opcje.addOption(pomoc);
        opcje.addOption(pokazFoldery);
        opcje.addOption(odrzucWynik);
        opcje.addOption(pokazDuplikaty);
        opcje.addOption(pokazPliki);
        opcje.addOption(noGUI);
        opcje.addOption(usunDuplikaty);
        opcje.addOption(znajdzDuplikaty);
        opcje.addOption(pokazMetodyPorownywania);
        opcje.addOption(pokazMinimalnePodobienstwo);
        opcje.addOption(nieSzukajDla);
        opcje.addOption(nieSzukajW);
        opcje.addOption(metodyPorownywania);
        opcje.addOption(szukajW);
        opcje.addOption(szukajDla);
        opcje.addOption(minimalnePodobienstwo);

        try {
            CommandLine cmd = parser.parse(opcje, args);

            if (cmd.hasOption("noGUI")) {

                System.out.println("Witaj w programie Duplikwidator!");
                System.out.println();
                System.out.println("Aby zapoznać się z dostępnymi komendami, wpisz polecenie \"pomoc\"");

            }

            if (cmd.hasOption("pomoc")) {

                System.out.println("-wyjscie - kończy pracę programu");

                System.out.println("-minimalnePodobienstwo [0-100] - ustawia minimalny procent podobieństwa na podaną wartość");

                System.out.println("-metodyPorownywania [pHash,radialHash,imgDiff] - wybiera metody porównywania plików graficznych");

                System.out.println("-szukajDla p [ścieżka w cudzysłowie] - dodaje plik lub zawartość folderu (argument p uwzględnia również podfoldery) do listy plików, dla których szukasz duplikatów");

                System.out.println("-nieSzukajDla w [ścieżka w cudzysłowie] - usuwa plik (lub wszystkie pliki przy użyciu argumentu w) z listy listy plików, dla których szukasz duplikatów");

                System.out.println("-szukajW p [ścieżka w cudzysłowie] - dodaje folderu (argument p uwzględnia również podfoldery) do listy miejsc, w których będą szukane duplikaty");

                System.out.println("-nieSzukajW w [ścieżka w cudzysłowie] - usuwa folder (lub wszystkie foldery przy użyciu argumentu w) z listy miejsc, w których będą szukane duplikaty");

                System.out.println("-znajdzDuplikaty - wyszukuje duplikaty plików graficznych");

                System.out.println("-odrzucWynik w [ID] - odrzuca wynik (lub wszystkie wyniki przy użyciu argumentu w) z listy znalezionych duplikatów ");

                System.out.println("-usunDuplikaty - usuwa duplikaty plików graficznych");

                System.out.println("-pokazDuplikaty - wyświetla listę duplikatów");

                System.out.println("-pokazFoldery - wyświetla listę folderów, w których szukasz duplikatów");

                System.out.println("-pokazMinimalnePodobienstwo - wyświetla stopień minimalnego podobieństwa plików do uznania ich za duplikaty");

                System.out.println("-pokazMetodyPorownywania - wyświetla metody wybrane do porównywania plików graficznych");

                System.out.println("-pokazPliki - wyświetla listę plików, dla których szukasz duplikatów");

            }

            if (cmd.hasOption("minimalnePodobienstwo")) {

                try {
                    String[] argumenty = cmd.getOptionValues("minimalnePodobienstwo");

                    for (String argument : argumenty) {
                        int procent = 0;

                        procent = Integer.parseInt(argument);

                        if (procent < 0 || procent > 100) {
                            System.out.println("Podano nieprawidłową wartość");

                        } else {
                            jSlider1.setValue(procent * 100);
                            System.out.println("Ustawiono minimalny procent podobieństwa: " + procent + "%");
                        }

                    }
                } catch (Exception e) {

                }

            }

            if (cmd.hasOption("metodyPorownywania")) {

                try {

                    jCheckBox1.setSelected(false);
                    jCheckBox3.setSelected(false);
                    jCheckBox4.setSelected(false);
                    ArrayList<String> wybrane_metody = new ArrayList<String>();
                    String[] argumenty = cmd.getOptionValues("metodyPorownywania");

                    for (String argument : argumenty) {

                        if (argument.equals("pHash") || argument.equals("imgDiff") || argument.equals("radialHash")) {

                            if (!wybrane_metody.contains(argument)) {
                                jCheckBox1.setSelected(true);
                                wybrane_metody.add(argument);
                            }

                        }

                    }

                    if (wybrane_metody.isEmpty()) {
                        System.out.println("Nie wybrano żadnej metody");
                    } else {

                        System.out.println("Wybrane metody: " + wybrane_metody.toString());

                    }
                } catch (Exception e) {

                }

            }

            if (cmd.hasOption("pokazMinimalnePodobienstwo")) {

                try {
                    System.out.println("Ustawiono minimalny procent podobieństwa: " + jTextField1.getText() + "%");
                } catch (Exception e) {

                }

            }

            if (cmd.hasOption("pokazMetodyPorownywania")) {

                try {
                    ArrayList<String> wybrane_metody = new ArrayList<String>();
                    wybrane_metody = new ArrayList<String>();

                    if (jCheckBox1.isSelected()) {

                        wybrane_metody.add("pHash");
                    }

                    if (jCheckBox3.isSelected()) {

                        wybrane_metody.add("radialHash");
                    }

                    if (jCheckBox4.isSelected()) {

                        wybrane_metody.add("imgDiff");
                    }

                    System.out.println("Wybrane metody: " + wybrane_metody.toString());
                } catch (Exception e) {

                }

            }

            if (cmd.hasOption("szukajDla")) {

                try {
                    String[] argumenty = cmd.getOptionValues("szukajDla");
   boolean podkatalogi = false;
                    for (String argument : argumenty) {

                     
                        if (argument.equals("p")) {
                            podkatalogi = true;
                        } else {

                            File file = new File(argument);
                            if (file.isDirectory()) {

                                File[] files = new File[1];
                                files[0] = file;

                                Main.dodaj_folder_pliki dodaj_folder_pliki = new Main.dodaj_folder_pliki(files, podkatalogi);

                                task_1 = new Thread(dodaj_folder_pliki);

                                zakoncz_watek_zmienna = 0;

                                task_1.start();
                                System.out.println("Trwa dodawanie plików...");

                                try {
                                    task_1.join();
                                } catch (InterruptedException e) {

                                }

                            } else if (file.isFile()) {

                                File[] files = new File[1];
                                files[0] = file;

                                for (File plik : files) {
                                    String sciezka = plik.getAbsolutePath();

                                    if (!model1.contains(sciezka)) {
                                        model1.addElement(sciezka);
                                    }

                                }

                                System.out.println("Operacja zakończona.");

                            } else {

                                System.out.println("Nie znaleziono podanej ścieżki");

                            }

                        }

                    }
                } catch (Exception e) {

                }

            }

            if (cmd.hasOption("nieSzukajDla")) {

                try {
                    String[] argumenty = cmd.getOptionValues("nieSzukajDla");

                    for (String argument : argumenty) {

                        if (argument.equals("w")) {
                            model1.removeAllElements();
                            break;
                        } else {

                            try {

                                model1.removeElement(argument);

                            } catch (Exception e) {
                                System.out.println("Podano nieprawidłową wartość.");
                            }

                        }

                    }
                    System.out.println("Operacja zakończona.");

                } catch (Exception e) {

                }

            }

            if (cmd.hasOption("pokazPliki")) {

                int ilosc_elementow_pliki = model1.getSize();

                for (int x = 0; x < ilosc_elementow_pliki; x++) {
                    System.out.println(model1.elementAt(x));
                }

            }

            if (cmd.hasOption("szukajW")) {

                try {
                    String[] argumenty = cmd.getOptionValues("szukajW");
    boolean podkatalogi = false;
                    for (String argument : argumenty) {

                    

                        if (argument.equals("p")) {

                            podkatalogi = true;

                        } else {

                            File file = new File(argument);
                            if (file.isDirectory()) {

                                File[] files = new File[1];
                                files[0] = file;

                                Main.dodaj_foldery dodaj_foldery = new Main.dodaj_foldery(files, podkatalogi);

                                task_1 = new Thread(dodaj_foldery);

                                zakoncz_watek_zmienna = 0;
                                task_1.start();
                                System.out.println("Trwa dodawanie folderów...");

                                try {
                                    task_1.join();
                                } catch (InterruptedException e) {

                                }

                            } else if (file.isFile()) {

                                System.out.println("Podano ścieżkę pliku zamiast folderu");

                            } else {

                                System.out.println("Nie znaleziono podanej ścieżki");

                            }

                        }

                    }
                } catch (Exception e) {

                }

            }

            if (cmd.hasOption("nieSzukajW")) {

                try {
                    String[] argumenty = cmd.getOptionValues("nieSzukajW");

                    for (String argument : argumenty) {

                        try {
                            if (argument.equals("w")) {
                                model2.removeAllElements();
                                break;
                            } else {

                                model2.removeElement(argument);

                            }

                        } catch (Exception e) {
                            System.out.println("Podano nieprawidłową wartość.");
                        }
                    }
                    System.out.println("Operacja zakończona.");

                } catch (Exception e) {

                }

            }

            if (cmd.hasOption("pokazFoldery")) {

                int ilosc_elementow_foldery = model2.getSize();

                for (int x = 0; x < ilosc_elementow_foldery; x++) {
                    System.out.println(model2.elementAt(x));
                }

            }

            if (cmd.hasOption("znajdzDuplikaty")) {

                boolean czyMozna = poprawnosc();
                if (czyMozna == true) {
                    Main.wyszukaj_duplikaty duplikaty = new Main.wyszukaj_duplikaty(10);
                    task_1 = new Thread(duplikaty);

                    int rowCount = model_tabeli.getRowCount();
                    for (int i = rowCount - 1; i >= 0; i--) {
                        model_tabeli.removeRow(i);
                    }

                    zakoncz_watek_zmienna = 0;
                    task_1.start();
                    System.out.println("Trwa wyszukiwanie duplikatów...");

                    try {
                        task_1.join();
                    } catch (InterruptedException e) {

                    }

                } else {

                    System.out.println("Nie określiłeś wszystkich parametrów wymaganych do wyszukania duplikatów");
                }

            }

            if (cmd.hasOption("pokazDuplikaty")) {

                int rowCount = model_tabeli.getRowCount();
                for (int i = 0; i < rowCount; i++) {

                    String pierwszy = model_tabeli.getValueAt(i, 0).toString();
                    String drugi = model_tabeli.getValueAt(i, 1).toString();
                    String wymiar = model_tabeli.getValueAt(i, 2).toString();
                    String pdobienstwo = model_tabeli.getValueAt(i, 3).toString();
                    System.out.println("ID: " + i);
                    System.out.println("Pierwszy plik: " + pierwszy);
                    System.out.println("Drugi plik: " + drugi);
                    System.out.println("wymiar: " + wymiar);
                    System.out.println("Podobienstwo: " + pdobienstwo);
                    System.out.println();
                }

            }

            if (cmd.hasOption("odrzucWynik")) {

                try {
                    String[] argumenty = cmd.getOptionValues("odrzucWynik");

                    for (String argument : argumenty) {

                        if (argument.equals("w")) {
                            int rowCount = model_tabeli.getRowCount();
                            for (int i = rowCount - 1; i >= 0; i--) {
                                model_tabeli.removeRow(i);
                            }
                            duplikaty_elementy.clear();
                            elementy.clear();

                            break;
                        } else {
                            try {
                                int wiersz = Integer.valueOf(argument);
                                String nazwa_p1 = model_tabeli.getValueAt(wiersz, 0).toString();
                                String nazwa_p2 = model_tabeli.getValueAt(wiersz, 1).toString();
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

                                model_tabeli.removeRow(wiersz);

                                System.out.println("Wynik odrzuony. ID pozostałych wyników zostały zmniejszone o 1.");
                            } catch (Exception e) {

                                System.out.println("Wystąpił błąd.");
                            }

                        }

                    }
                    System.out.println("Operacja zakończona.");

                } catch (Exception e) {

                }

            }

            if (cmd.hasOption("usunDuplikaty")) {

                if (model_tabeli.getRowCount() > 0) {

                    Main.usun_duplikaty usun_duplikaty = new Main.usun_duplikaty(0);
                    task_1 = new Thread(usun_duplikaty);

                    zakoncz_watek_zmienna = 0;
                    task_1.start();
                    System.out.println("Trwa usuwanie duplikatów...");

                    try {
                        task_1.join();
                    } catch (InterruptedException e) {

                    }

                } else {
                    System.out.println("Lista duplikatów jest pusta.");
                }

            }

            if (cmd.hasOption("wyjscie")) {

                System.exit(0);

            } else {

                String a = odczytaj.nextLine();
                String[] array = a.split(" ");

                cmd(array);

            }

        } catch (ParseException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
