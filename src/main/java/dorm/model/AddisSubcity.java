package dorm.model;

//Addis Ababa subcities with their woreda counts.

public enum AddisSubcity {
    ADDIS_KETEMA("Addis Ketema", 14),
    AKAKI_KALITI("Akaki Kaliti", 13),
    ARADA("Arada", 10),
    BOLE("Bole", 15),
    GULLELE("Gullele", 10),
    KIRKOS("Kirkos", 11),
    KOLFE_KERANIYO("Kolfe Keraniyo", 15),
    LEMI_KURA("Lemi Kura", 14),
    LIDETA("Lideta", 10),
    NIFAS_SILK_LAFTO("Nifas Silk Lafto", 15),
    YEKA("Yeka", 13);

    private final String displayName;
    private final int woredaCount;

    AddisSubcity(String displayName, int woredaCount) {
        this.displayName = displayName;
        this.woredaCount = woredaCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getWoredaCount() {
        return woredaCount;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
