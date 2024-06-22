import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final HttpClient httpClient;
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger requestCount;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newHttpClient();
        this.semaphore = new Semaphore(requestLimit);
        this.requestCount = new AtomicInteger(0);
        long intervalMillis = timeUnit.toMillis(1);

        this.scheduler = Executors.newScheduledThreadPool(1);
        this.scheduler.scheduleAtFixedRate(() -> {
            semaphore.release(requestLimit - semaphore.availablePermits());
            requestCount.set(0);
        }, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    public void createDocument(Document document, String signature) throws IOException, InterruptedException {
        semaphore.acquire();

        String jsonPayload = document.toJson();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to create document: " + response.body());
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException ex) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static class Document {
        private Description description;
        private String docId;
        private String docStatus;
        private boolean importRequest;
        private String ownerInn;
        private String participantInn;
        private String producerInn;
        private String productionDate;
        private String productionType;
        private Product[] products;
        private String regDate;
        private String regNumber;

        // Getters and setters
        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getDocStatus() {
            return docStatus;
        }

        public void setDocStatus(String docStatus) {
            this.docStatus = docStatus;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

        public String getProductionType() {
            return productionType;
        }

        public void setProductionType(String productionType) {
            this.productionType = productionType;
        }

        public Product[] getProducts() {
            return products;
        }

        public void setProducts(Product[] products) {
            this.products = products;
        }

        public String getRegDate() {
            return regDate;
        }

        public void setRegDate(String regDate) {
            this.regDate = regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }

        public String toJson() {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"description\":").append(description != null ? description.toJson() : "{}").append(",");
            json.append("\"doc_id\":\"").append(docId).append("\",");
            json.append("\"doc_status\":\"").append(docStatus).append("\",");
            json.append("\"doc_type\":\"LP_INTRODUCE_GOODS\",");
            json.append("\"importRequest\":").append(importRequest).append(",");
            json.append("\"owner_inn\":\"").append(ownerInn).append("\",");
            json.append("\"participant_inn\":\"").append(participantInn).append("\",");
            json.append("\"producer_inn\":\"").append(producerInn).append("\",");
            json.append("\"production_date\":\"").append(productionDate).append("\",");
            json.append("\"production_type\":\"").append(productionType).append("\",");
            json.append("\"products\":").append(products != null ? productsToJson() : "[]").append(",");
            json.append("\"reg_date\":\"").append(regDate).append("\",");
            json.append("\"reg_number\":\"").append(regNumber).append("\"");
            json.append("}");
            return json.toString();
        }

        private String productsToJson() {
            StringBuilder json = new StringBuilder();
            json.append("[");
            if (products != null) {
                for (int i = 0; i < products.length; i++) {
                    json.append(products[i].toJson());
                    if (i < products.length - 1) {
                        json.append(",");
                    }
                }
            }
            json.append("]");
            return json.toString();
        }

        public static class Description {
            private String participantInn;

            // Getters and setters
            public String getParticipantInn() {
                return participantInn;
            }

            public void setParticipantInn(String participantInn) {
                this.participantInn = participantInn;
            }

            public String toJson() {
                return "{\"participantInn\":\"" + participantInn + "\"}";
            }
        }

        public static class Product {
            private String certificateDocument;
            private String certificateDocumentDate;
            private String certificateDocumentNumber;
            private String ownerInn;
            private String producerInn;
            private String productionDate;
            private String tnvedCode;
            private String uitCode;
            private String uituCode;

            // Getters and setters
            public String getCertificateDocument() {
                return certificateDocument;
            }

            public void setCertificateDocument(String certificateDocument) {
                this.certificateDocument = certificateDocument;
            }

            public String getCertificateDocumentDate() {
                return certificateDocumentDate;
            }

            public void setCertificateDocumentDate(String certificateDocumentDate) {
                this.certificateDocumentDate = certificateDocumentDate;
            }

            public String getCertificateDocumentNumber() {
                return certificateDocumentNumber;
            }

            public void setCertificateDocumentNumber(String certificateDocumentNumber) {
                this.certificateDocumentNumber = certificateDocumentNumber;
            }

            public String getOwnerInn() {
                return ownerInn;
            }

            public void setOwnerInn(String ownerInn) {
                this.ownerInn = ownerInn;
            }

            public String getProducerInn() {
                return producerInn;
            }

            public void setProducerInn(String producerInn) {
                this.producerInn = producerInn;
            }

            public String getProductionDate() {
                return productionDate;
            }

            public void setProductionDate(String productionDate) {
                this.productionDate = productionDate;
            }

            public String getTnvedCode() {
                return tnvedCode;
            }

            public void setTnvedCode(String tnvedCode) {
                this.tnvedCode = tnvedCode;
            }

            public String getUitCode() {
                return uitCode;
            }

            public void setUitCode(String uitCode) {
                this.uitCode = uitCode;
            }

            public String getUituCode() {
                return uituCode;
            }

            public void setUituCode(String uituCode) {
                this.uituCode = uituCode;
            }

            public String toJson() {
                return "{" +
                        "\"certificate_document\":\"" + certificateDocument + "\"," +
                        "\"certificate_document_date\":\"" + certificateDocumentDate + "\"," +
                        "\"certificate_document_number\":\"" + certificateDocumentNumber + "\"," +
                        "\"owner_inn\":\"" + ownerInn + "\"," +
                        "\"producer_inn\":\"" + producerInn + "\"," +
                        "\"production_date\":\"" + productionDate + "\"," +
                        "\"tnved_code\":\"" + tnvedCode + "\"," +
                        "\"uit_code\":\"" + uitCode + "\"," +
                        "\"uitu_code\":\"" + uituCode + "\"" +
                        "}";
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        CrptApi api = new CrptApi(TimeUnit.MINUTES, 10);
        Document doc = new Document();
        Document.Description description = new Document.Description();
        description.setParticipantInn("1234567890");
        doc.setDescription(description);
        doc.setDocId("doc123");
        doc.setDocStatus("NEW");
        doc.setImportRequest(true);
        doc.setOwnerInn("ownerInn123");
        doc.setParticipantInn("participantInn123");
        doc.setProducerInn("producerInn123");
        doc.setProductionDate("2023 - 06 - 01");
        doc.setProductionType("TYPE1");
        doc.setRegDate("2023 - 06 - 01");
        doc.setRegNumber("reg123");
        Document.Product product = new Document.Product();
        product.setCertificateDocument("certDoc123");
        product.setCertificateDocumentDate("2023-06-01");
        product.setCertificateDocumentNumber("certNum123");
        product.setOwnerInn("ownerInn123");
        product.setProducerInn("producerInn123");
        product.setProductionDate("2023-06-01");
        product.setTnvedCode("tnvedCode123");
        product.setUitCode("uitCode123");
        product.setUituCode("uituCode123");

        doc.setProducts(new Document.Product[]{product});

        api.createDocument(doc, "signature");
        api.shutdown();
    }
}