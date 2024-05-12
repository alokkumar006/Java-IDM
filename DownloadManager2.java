import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class DownloadManager2 {
    private static final int BUFFER_SIZE = 4096;
    private static final String DOWNLOADS_FOLDER = "D:\\alok\\";

    private List<DownloadTask> downloadQueue;

    public DownloadManager2() {
        downloadQueue = new ArrayList<>();
    }

    public void addDownload(String url, String fileName) {
        DownloadTask task = new DownloadTask(url, fileName);
        downloadQueue.add(task);
    }

    public void startDownloads() {
        for (DownloadTask task : downloadQueue) {
            new Thread(task).start();
        }
    }

    public void pauseDownloads() {
        for (DownloadTask task : downloadQueue) {
            task.pauseDownload();
        }
    }

    public void resumeDownloads() {
        for (DownloadTask task : downloadQueue) {
            task.resumeDownload();
        }
    }

    private static class DownloadTask implements Runnable {
        private String url;
        private String fileName;
        private volatile boolean paused;

        public DownloadTask(String url, String fileName) {
            this.url = url;
            this.fileName = fileName;
            this.paused = false;
        }

        public void pauseDownload() {
            paused = true;
        }

        public void resumeDownload() {
            paused = false;
            synchronized (this) {
                notify();
            }
        }

        @Override
        public void run() {
            try {
                URL fileUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    int fileSize = connection.getContentLength();

                    try (InputStream inputStream = connection.getInputStream();
                         FileOutputStream outputStream = new FileOutputStream(DOWNLOADS_FOLDER + fileName)) {

                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        int totalBytesRead = 0;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            synchronized (this) {
                                while (paused) {
                                    wait();
                                }
                            }

                            outputStream.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            System.out.print("\rDownloading " + fileName + ": " +
                                    totalBytesRead + " / " + fileSize + " bytes");
                        }

                        System.out.println("\nDownload complete: " + fileName);

                    } catch (IOException e) {
                        System.err.println("Error while downloading " + fileName);
                        e.printStackTrace();
                    } catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

                } else {
                    System.err.println("Failed to connect to the server. Response code: " + responseCode);
                }

            } catch (IOException e) {
                System.err.println("Error while connecting to the server");
                e.printStackTrace();
            }
            System.out.println("File saved at: " + new File(DOWNLOADS_FOLDER + fileName).getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        DownloadManager2 DownloadManager2 = new DownloadManager2();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. Add Download");
            System.out.println("2. Start Downloads");
            System.out.println("3. Pause Downloads");
            System.out.println("4. Resume Downloads");
            System.out.println("5. Exit");

            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter URL: ");
                    String url = scanner.next();
                    System.out.print("Enter file name: ");
                    String fileName = scanner.next();
                    DownloadManager2.addDownload(url, fileName);
                    break;
                case 2:
                    DownloadManager2.startDownloads();
                    break;
                case 3:
                    DownloadManager2.pauseDownloads();
                    break;
                case 4:
                    DownloadManager2.resumeDownloads();
                    break;
                case 5:
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
