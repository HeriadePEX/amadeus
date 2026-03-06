# Actions et entites metier (detail WSDL)

Source principale: `docs/openapi.yaml` + schemas WSDL/XSD sous `src/main/resources/wsdl/`.
Note: les structures SOAP sont vastes; ce fichier detaille les champs racines et les types communs utilises dans les requetes REST. Pour des sous-types non detailles, voir les fichiers XSD cites.

## Create Profile (POST /api/profiles)

| Entite | Champs | Cardinalite | Commentaire |
| --- | --- | --- | --- |
| SoapRequest | payload, session | payload requis | Enveloppe REST. |
| AMA_ProfileCreateRQ (payload) | UniqueID, ExternalID, Profile, MidOffice + attributs OTA_PayloadStdAttributes | UniqueID 0..5, ExternalID 0..25, Profile 1, MidOffice 0..1 | `AMA/2012U/AMA_ProfileCreateRQ.xsd`. |
| UniqueID_Type | @Type, @ID, @Instance, @ID_Context, @RPH | @Type et @ID requis | Identifiant unique (voir types communs). |
| ExternalID_Type | inherits UniqueID_Type + @Access_Token, @Access_Token_Secret, @ApplicationOwnerID, @ApplicationOwnerKey, @Timestamp, @NotifLink | 0..1 par attribut | Identifiant externe + tokens. |
| ProfileType | Accesses, Customer, UserID, PrefCollections, CompanyInfo, Affiliations, Agreements, Comments, CustomFields, SocialNetworks, Notifications, NoxIDs + attributs (Locked, ProfileType, CreatorID, ActionDate, RPH, Status, NameMatchingContext) | elements 0..n, attributs optionnels | Racine du contenu profil (`AMA/2012U/AMA_Profile.xsd`). |
| MidOfficeType | VATExemptedReason, FeeModel, Legalname, CancellationConditions, AccountancyNumber, PaymentPref, InvoicePref, Document, URL, ProcessingFields + attributs (SupplierType, Voucher, RPH) | elements 0..n | Section mid-office (`AMA/2012U/AMA_Profile.xsd`). |
| SessionContext | sessionId, sequenceNumber, securityToken, transactionStatusCode | optionnel | Contexte de session REST (OpenAPI). |

## Update Profile (PUT /api/profiles)

| Entite | Champs | Cardinalite | Commentaire |
| --- | --- | --- | --- |
| SoapRequest | payload, session | payload requis | Enveloppe REST. |
| AMA_UpdateRQ (payload) | UniqueID, ExternalID, Position + attributs OTA_PayloadStdAttributes | UniqueID 0..5, ExternalID 0..25, Position 1 | `AMA/2012U/AMA_UpdateRQ.xsd`. |
| UpdatePositionType | Root (@Operation, UniqueID, ExternalID, Profile, MidOffice) + @XPath | Root 1 | `AMA/2012U/AMA_ProfileUpdateTypes.xsd`. |
| Root (UpdatePositionType) | UniqueID, ExternalID, Profile, MidOffice + @Operation | elements 0..n | Operation: replace/createupdate. |
| UniqueID_Type | @Type, @ID, @Instance, @ID_Context, @RPH | @Type et @ID requis | Identifiant unique. |
| ExternalID_Type | inherits UniqueID_Type + @Access_Token, @Access_Token_Secret, @ApplicationOwnerID, @ApplicationOwnerKey, @Timestamp, @NotifLink | 0..1 par attribut | Identifiant externe + tokens. |
| ProfileType | Accesses, Customer, UserID, PrefCollections, CompanyInfo, Affiliations, Agreements, Comments, CustomFields, SocialNetworks, Notifications, NoxIDs + attributs | elements 0..n | Contenu profil. |
| MidOfficeType | VATExemptedReason, FeeModel, Legalname, CancellationConditions, AccountancyNumber, PaymentPref, InvoicePref, Document, URL, ProcessingFields + attributs | elements 0..n | Section mid-office. |
| SessionContext | sessionId, sequenceNumber, securityToken, transactionStatusCode | optionnel | Contexte de session REST. |

## Delete Profile (DELETE /api/profiles)

| Entite | Champs | Cardinalite | Commentaire |
| --- | --- | --- | --- |
| SoapRequest | payload, session | payload requis | Enveloppe REST. |
| AMA_DeleteRQ (payload) | UniqueID, ExternalID, DeleteRequests + attributs OTA_PayloadStdAttributes/ReqRespVersion | UniqueID 0..5, ExternalID 0..25, DeleteRequests 1 | `AMA/2012U/AMA_DeleteRQ.xsd`. |
| UniqueID_Type | @Type, @ID, @Instance, @ID_Context, @RPH | @Type et @ID requis | Identifiant unique. |
| UniqueID (DeleteRQ) | RelatedProfile (UniqueID_Type) | 0..1 | UniqueID peut contenir un profil lie. |
| DeleteRequests | ProfileDeleteRequest (@ProfileType) | 1 | Indique le type de profil a supprimer. |
| SessionContext | sessionId, sequenceNumber, securityToken, transactionStatusCode | optionnel | Contexte de session REST. |

## Read Profile (POST /api/profiles/search)

| Entite | Champs | Cardinalite | Commentaire |
| --- | --- | --- | --- |
| SoapRequest | payload, session | payload requis | Enveloppe REST. |
| AMA_ProfileReadRQ (payload) | UniqueID, ExternalID, LastMatchingProfileInfo, ReadRequests + attributs (ResponseGroup, OTA_PayloadStdAttributes) | UniqueID 0..999, ExternalID 0..25, ReadRequests 0..1 | `AMA/2012U/AMA_ProfileReadRQ.xsd`. |
| ReadRequests | ProfileReadRequest | 0..1 | Conteneur de lecture. |
| ProfileReadRequest | FreeTextSearch OR (Customer, UserID, CompanyInfo, Agreements, PrefCollections, MidOffice) OR UniqueID + attributs (Locked, ProfileType, CreatorID, Status) | choix 1 | Filtre de recherche. |
| UniqueID_Type | @Type, @ID, @Instance, @ID_Context, @RPH | @Type et @ID requis | Identifiant unique. |
| CustomerType | PersonName, Telephone, Email, Address, URL, CitizenCountryName, PaymentForm, RelatedTraveler, ContactPerson, Document, CustLoyalty, EmployeeInfo, LanguageSpoken, EmployerInfo, FamilyInfo, LivingLocation, Rates, TravelCategories, Vehicle, FlightInfo, LoungeCoupons + attributs (CustomerValue, Gender, BirthDate, VIP_Indicator, CurrencyCode, BirthCountry, LockedInd, OutOfOfficeInd, ...) | elements 0..n | Details client (voir `AMA/2012U/AMA_ProfileCommonTypes.xsd`). |
| CompanyInfoType | CompanyName, OtherCompanyName, AddressInfo, URL, TelephoneInfo, Email, PaymentForm, TravelArranger, LoyaltyProgram, ContactPerson, Rates, Location, TravelCategories, LocationCountryName, Document + attributs (Type, CurrencyCode) | elements 0..n | Infos societe (`AMA/2012U/AMA_Profile.xsd`). |
| AgreementsType | CommissionInfo, MarginInfo, DiscountInfo, ContractInformation, OwnerRights, ReceptorRights, LegalStatement | elements 0..n | Accords commerciaux. |
| PreferencesType | PrefCollection (PreferenceType) | 1..2500 | Collections de preferences. |
| MidOfficeType | VATExemptedReason, FeeModel, Legalname, CancellationConditions, AccountancyNumber, PaymentPref, InvoicePref, Document, URL, ProcessingFields + attributs | elements 0..n | Section mid-office. |
| SessionContext | sessionId, sequenceNumber, securityToken, transactionStatusCode | optionnel | Contexte de session REST. |

## List Deactivated Profiles (POST /api/profiles/deactivated)

Pagination REST:
- Query params optionnels: `page` (>=1, defaut 1), `size` (1..150, defaut 50).
- Reponse REST: `payload`, `session`, `pagination{page,size,returnedCount,hasNext,nextCursor}`.

| Entite | Champs | Cardinalite | Commentaire |
| --- | --- | --- | --- |
| SoapRequest | payload, session | payload requis | Enveloppe REST. |
| Profile_ListDeactivatedProfiles (payload) | messageActionDetails, retrieveProfileDomain, inputIndicator, profileIdentificationSection | 1..1 | `Profile_ListDeactivatedProfiles_01_1_1A.xsd`. |
| messageActionDetails | messageDetails | 1 | Action demandee. |
| messageDetails | function, codeListResponsibleAgency | 1 | function: an..3. |
| retrieveProfileDomain | profileOwner, retrieveDomain, followUpDisplayDate, deactivatedDate | 0..1 | Criteres de filtre. |
| profileOwner | officeID | 0..1 | officeID: an9. |
| retrieveDomain | corporateIdentification | 0..1 | corporateIdentification: an3. |
| inputIndicator | numberOfLinesToBeReturned, newCompanyProfile | 0..1 | Limites d'affichage. |
| profileIdentificationSection | profileIdentification (recordLocator, profileType) | 0..1 | Filtre sur profil. |
| SessionContext | sessionId, sequenceNumber, securityToken, transactionStatusCode | optionnel | Contexte de session REST. |

## Read Profile (POST /api/profiles/search)

Pagination REST:
- Query params optionnels: `page` (>=1, defaut 1), `size` (1..150, defaut 50).
- Reponse REST: `payload`, `session`, `pagination{page,size,returnedCount,hasNext,nextCursor}`.

## Sign Out (DELETE /api/sessions)

Note: l'exemple OpenAPI utilise `OriginatorDetails/InHouseIdentification1`, mais le WSDL defini `conversationClt`.

| Entite | Champs | Cardinalite | Commentaire |
| --- | --- | --- | --- |
| SoapRequest | payload, session | payload requis | Enveloppe REST. |
| Security_SignOut (payload) | conversationClt | 0..1 | `Security_SignOut_04_1_1A.xsd`. |
| conversationClt | senderIdentification, recipientIdentification, senderInterchangeControlReference, recipientInterchangeControlReference | 1 | Propriete de conversation. |
| SessionContext | sessionId, sequenceNumber, securityToken, transactionStatusCode | optionnel | Contexte de session REST. |

## Types communs (extraits)

| Type | Champs | Commentaire |
| --- | --- | --- |
| UniqueID_Type | @Type (required), @ID (required), @Instance, @ID_Context, @RPH | `AMA/2012U/AMA_ProfileCommonTypes.xsd`. @Type est enum (1,4,5,9,21,91,101,105,106,107,108...). |
| ExternalID_Type | UniqueID_Type + @Access_Token, @Access_Token_Secret, @ApplicationOwnerID, @ApplicationOwnerKey, @Timestamp, @NotifLink | Tokens d'acces pour identifiants externes. |
| OTA_PayloadStdAttributes | @EchoToken, @Version (required), @PrimaryLangID, @Usage | Attributs standard OTA sur les racines. |
| ResponseGroup | @MoreIndicator, @MoreDataEchoToken, @MaxResponses | Utilise dans AMA_ProfileReadRQ. |
| ProfileTypeGroup | @ProfileType | Typage profil (Traveller/Company/Corporate/etc). |
| DateTimeStampGroup | @CreatorID, @ActionDate | Creation/horodatage. |
| UpdatePositionType | Root (@Operation: replace/createupdate), @XPath | Mise a jour avec chemin XPath. |
| PartialUpdatePositionType | Root (@Operation: update/delete/mileageupdate), @XPath | Utilise pour updates partielles. |
