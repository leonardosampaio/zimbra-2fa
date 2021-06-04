package br.com.sampaio.auth;

public enum ProtocolType {

    OTHER("other"),
    SOAP("soap"),
    POP3("pop3"),
    SMTP("stmp"),
    IMAP("imap");

    private final String description;

    ProtocolType(String description)
    {
        this.description = description;
    }

    public static ProtocolType getByDescription(String description) {
        if (description != null && !description.isEmpty())
        {
            for (ProtocolType l : ProtocolType.values()) {
                if (l.description.equals(description))
                {
                    return l;
                }
            }
        }
        return ProtocolType.OTHER;
     }

     public String getDescription()
     {
        return description;
     }
}
