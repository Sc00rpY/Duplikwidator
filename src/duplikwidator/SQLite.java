package duplikwidator;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.io.FilenameUtils;

/**
 * Klasa odpowiadająca za operacje na bazie danych SQLite.
 *
 */
public class SQLite extends Main {

    boolean czyPolaczono;
    String sciezkaDB = " ";

    /**
     * Połączenie z bazą danych SQLite
     *
     * Metoda nawiązuje połączenie z bazą SQLite.
     *
     * @param baza_danych adres URL bazy danych
     * @return conn zmienna zawierająca informacje o stanie połączenia
     */
    public static Connection connect_sqlite(String baza_danych) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + baza_danych);

        } catch (SQLException e) {
            //  System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Aktualizacja wiersza bazy danych SQLite
     *
     * Metoda aktualizuje zawartość wiersza zawierającego informacje o pliku
     * graficznym w bazie danych. W przypadku braku połączenia z bazą danych
     * metoda próbuje nawiązać nowe połączenie i utworzyć nową tablicę, jeżeli
     * baza jest pusta.
     *
     * @param sciezka_pliku_2 ścieżka bezwzględna do pliku graficznego.
     * @param kolumna nazwa kolumny, której wartość zostanie zmieniona
     * @param wartosc nowa wartość wstawiana do wiersza
     */
    public void aktualizujWiersz(String sciezka_pliku_2, String kolumna, String wartosc) {

        try {
            File plik_porownywany_file = new File(sciezka_pliku_2);

            String sciezka_pliku = FilenameUtils.getFullPathNoEndSeparator(plik_porownywany_file.getAbsolutePath());

            if (polaczenieDB == null) {
                polaczenieDB = connect_sqlite(sciezka_pliku + "/duplikwidator_db.db");
                utworz_tablice();
                sciezkaDB = sciezka_pliku;
            } else {

                if (!sciezkaDB.equals(sciezka_pliku)) {

                    czyPolaczono = polaczenieDB.isValid(30);

                    if (czyPolaczono == false) {

                        polaczenieDB = connect_sqlite(sciezka_pliku + "/duplikwidator_db.db");
                        utworz_tablice();
                        sciezkaDB = sciezka_pliku;
                    } else {
                        polaczenieDB.close();

                        polaczenieDB = connect_sqlite(sciezka_pliku + "/duplikwidator_db.db");
                        utworz_tablice();
                        sciezkaDB = sciezka_pliku;
                    }

                }

            }

            czyPolaczono = polaczenieDB.isValid(30);

            if (czyPolaczono == true) {

                String sql = "UPDATE duplikwidator SET " + kolumna + " = '" + wartosc + "'"
                        + "WHERE sciezka = '" + sciezka_pliku_2 + "'";

                try (
                        Statement stmt = polaczenieDB.createStatement()) {

                    // stmt.executeUpdate(sql);
                    stmt.executeUpdate(sql);
                } catch (SQLException e) {
                    //  System.out.println(e.getMessage());
                }

            }

        } catch (Exception e) {

        }
    }

    /**
     * Wstawianie wiersza do bazy danyh SQLite
     *
     * Metoda wstawia nowy wiersz do bazy zawierającej informacje o plikach
     * graficznym. W przypadku braku połączenia z bazą danych metoda próbuje
     * nawiązać nowe połączenie i utworzyć nową tablicę, jeżeli baza jest pusta.
     *
     * @param sciezka_pliku_2 ścieżka bezwzględna do pliku graficznego
     * @param jphash_hash hash pliku graficznego zwrócony przez funkcję jpHash
     * @param phash_hash hash pliku graficznego zwrócony przez funkcję
     * radialHash
     * @param crc wartość sumy kontrolnej pliku graficznego
     * @param rozdzielczosc rozdzielczość pliku graficznego
     */
    public void wstawWiersz(String sciezka_pliku_2, String jphash_hash, String phash_hash, String crc, String rozdzielczosc) {

        try {
            File plik_porownywany_file = new File(sciezka_pliku_2);

            String sciezka_pliku = FilenameUtils.getFullPathNoEndSeparator(plik_porownywany_file.getAbsolutePath());

            if (polaczenieDB == null) {
                polaczenieDB = connect_sqlite(sciezka_pliku + "/duplikwidator_db.db");
                utworz_tablice();
                sciezkaDB = sciezka_pliku;
            } else {

                if (!sciezkaDB.equals(sciezka_pliku)) {

                    czyPolaczono = polaczenieDB.isValid(30);

                    if (czyPolaczono == false) {

                        polaczenieDB = connect_sqlite(sciezka_pliku + "/duplikwidator_db.db");
                        utworz_tablice();
                        sciezkaDB = sciezka_pliku;
                    } else {
                        polaczenieDB.close();
                        polaczenieDB = connect_sqlite(sciezka_pliku + "/duplikwidator_db.db");
                        utworz_tablice();
                        sciezkaDB = sciezka_pliku;
                    }

                }

            }

            czyPolaczono = polaczenieDB.isValid(30);

            if (czyPolaczono == true) {

                String sql2 = "INSERT INTO duplikwidator(sciezka,jphash_hash, phash_hash, hash_pliku, rozdzielczosc) VALUES('" + sciezka_pliku_2 + "','" + jphash_hash + "','" + phash_hash + "','" + crc + "','" + rozdzielczosc + "')";

                try (
                        Statement stmt = polaczenieDB.createStatement()) {

                    // stmt.executeUpdate(sql);
                    stmt.executeUpdate(sql2);
                } catch (SQLException e) {
                    // System.out.println(e.getMessage());
                }

            }

        } catch (Exception e) {

        }
    }

    /**
     * Zwracanie informacji z bazy danych SQLite
     *
     * Metoda zwraca informacje o pliku graficznym w bazy SQLite. W przypadku
     * braku połączenia z bazą danych metoda próbuje nawiązać nowe połączenie i
     * utworzyć nową tablicę, jeżeli baza jest pusta.
     *
     * @param plik_szukany ścieżka bezwzględna do pliku graficznego
     * @return wynik tablica obiektów zawierających informacje o pliku
     * graficznym pozyskane z bazy danych
     */
    public Object[] sprawdzBaze(String plik_szukany) {
        Object[] wynik = new String[6];
        try {
            File plik_porownywany_file = new File(plik_szukany);

            String sciezka_pliku = FilenameUtils.getFullPathNoEndSeparator(plik_porownywany_file.getAbsolutePath());

            if (polaczenieDB == null) {

                polaczenieDB = connect_sqlite(sciezka_pliku + "/duplikwidator_db.db");
                utworz_tablice();
                sciezkaDB = sciezka_pliku;
            } else {

                if (!sciezkaDB.equals(sciezka_pliku)) {

                    czyPolaczono = polaczenieDB.isValid(30);

                    if (czyPolaczono == false) {

                        polaczenieDB = connect_sqlite(sciezka_pliku + "/duplikwidator_db.db");
                        utworz_tablice();
                        sciezkaDB = sciezka_pliku;
                    } else {

                        polaczenieDB.close();
                        polaczenieDB = connect_sqlite(sciezka_pliku + "/duplikwidator_db.db");
                        utworz_tablice();
                        sciezkaDB = sciezka_pliku;
                    }

                } else {
                    czyPolaczono = polaczenieDB.isValid(30);

                    if (czyPolaczono == false) {
                        polaczenieDB = connect_sqlite(sciezka_pliku + "/duplikwidator_db.db");
                        utworz_tablice();
                    }

                }

            }

            czyPolaczono = polaczenieDB.isValid(30);

            if (czyPolaczono == true) {

                String sql3 = "SELECT id, sciezka, jphash_hash, phash_hash, hash_pliku, rozdzielczosc FROM duplikwidator WHERE sciezka = '" + plik_szukany + "'";

                try (
                        Statement stmt = polaczenieDB.createStatement()) {

                    // stmt.executeUpdate(sql);
                    ResultSet ha = stmt.executeQuery(sql3);
                    while (ha.next()) {
                        wynik[0] = ha.getString("id");
                        wynik[1] = ha.getString("sciezka");
                        wynik[2] = ha.getString("jphash_hash");
                        wynik[3] = ha.getString("phash_hash");
                        wynik[4] = ha.getString("hash_pliku");
                        wynik[5] = ha.getString("rozdzielczosc");
                    }
                } catch (SQLException e) {
                    //  System.out.println(e.getMessage());
                }

            }

        } catch (Exception e) {

        }
        return wynik;
    }

    /**
     * Tworzenie tablicy w bazie danych SQLite
     *
     * Metoda tworzy tablicę w bazie danych SQLite zawierającą informację o
     * plikach graficznych.
     */
    public void utworz_tablice() {

        String sql1 = "CREATE TABLE IF NOT EXISTS duplikwidator (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	sciezka text NOT NULL,\n"
                + "	jphash_hash text NOT NULL,\n"
                + "	phash_hash text NOT NULL,\n"
                + "	hash_pliku text NOT NULL,\n"
                + "	rozdzielczosc text NOT NULL"
                + ");";

        try (
                Statement stmt = polaczenieDB.createStatement()) {
            stmt.executeUpdate(sql1);
        } catch (SQLException e) {
            //  System.out.println(e.getMessage());
        }

    }

}
