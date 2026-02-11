# Amadeus REST API (tests profils)

## 1) Creation

```bash
curl -X POST "http://localhost:8080/api/profiles" \
  -H "Content-Type: application/json" \
  -d '{
    "payload": {
      "version": 12.2,
      "uniqueID": [
        {
          "type": "9",
          "ID": "PARFT2796",
          "IDContext": "CSX"
        }
      ],
      "profile": {
        "profileType": "3",
        "status": "A",
        "companyInfo": {
          "companyName": {
            "value": "TEST COMPANY NAME 2"
          },
          "addressInfo": [
            {
              "formattedInd": true,
              "defaultInd": true,
              "useType": "2",
              "addressLine": [
                "55 STREET"
              ],
              "cityName": "London",
              "countryName": {
                "code": "GB"
              },
              "addresseeName": {
                "surname": "CLAIRE"
              }
            }
          ],
          "telephoneInfo": [
            {
              "phoneLocationType": "6",
              "phoneTechType": "1",
              "phoneNumber": "NCE12345678"
            }
          ]
        }
      }
    }
  }'
```

## 2) Recherche (par ID)

```bash
curl -X POST "http://localhost:8080/api/profiles/search" \
  -H "Content-Type: application/json" \
  -d '{
    "payload": {
      "version": 12.2,
      "uniqueID": [
        {
          "type": "9",
          "ID": "PARFT2796",
          "IDContext": "CSX"
        }
      ],
      "readRequests": {
        "profileReadRequest": {
          "profileType": "3",
          "uniqueID": {
            "type": "21",
            "ID": "26K4QM",
            "IDContext": "CSX",
            "instance": "1"
          }
        }
      }
    }
  }'
```

## 3) Mise a jour

```bash
curl -X PUT "http://localhost:8080/api/profiles" \
  -H "Content-Type: application/json" \
  -d '{
    "payload": {
      "version": 12.2,
      "uniqueID": [
        {
          "type": "9",
          "ID": "PARFT2796",
          "IDContext": "CSX"
        }
      ],
      "position": {
        "xPath": "/Profile",
        "root": {
          "operation": "replace",
          "uniqueID": [
            {
              "type": "21",
              "ID": "26K4QM",
              "IDContext": "CSX",
              "instance": "1"
            }
          ],
          "profile": {
            "profileType": "3",
            "companyInfo": {
              "companyName": {
                "value": "UPDATED COMPANY NAME"
              }
            }
          }
        }
      }
    }
  }'
```
