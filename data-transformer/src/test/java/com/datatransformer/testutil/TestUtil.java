package com.datatransformer.testutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtil {

    public static void createSampleJsonFile(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    public static String getSampleRawUserJson() {
        return """
                {
                    "value": [
                        {
                            "id": "1",
                            "userPrincipalName": "user1@example.com",
                            "mail": "user1@example.com",
                            "userType": "Member",
                            "usageLocation": "US",
                            "accountEnabled": true,
                            "givenName": "User",
                            "surname": "One",
                            "signInActivity": {
                                "lastSignInDateTime": "1979-06-05T20:27:13",
                                "lastSignInRequestId": "358c844b-57c8-4edd-9349-3fd6d30856d9",
                                "lastNonInteractiveSignInDateTime": "1988-02-23T11:58:58",
                                "lastNonInteractiveSignInRequestId": "364227ea-bd93-4d35-aa57-2ac4ba4ef343",
                                "lastSuccessfulSignInDateTime": "1971-09-11T07:04:30",
                                "lastSuccessfulSignInRequestId": "f26ffd32-0171-456c-a1e4-3abb6edbd215"
                            }
                        }
                    ]
                }
                """;
    }
}
