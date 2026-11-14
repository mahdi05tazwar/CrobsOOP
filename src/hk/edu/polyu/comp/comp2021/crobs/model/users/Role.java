package hk.edu.polyu.comp.comp2021.crobs.model.users;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;

/**
 * Represents the role of a user in the system
 */
public enum Role {
    /**
     * regular role
     */
    REGULAR("regular"),
    /**
     * admin role
     */
    ADMIN("admin"),
    /**
     * superadmin role
     */
    SUPER_ADMIN("superadmin");

    private final String keyword;

    Role(String keyword) {
        this.keyword = keyword;
    }

    /**
     * @return the keyword string for this role
     */
    public String getKeyword() {
       return this.keyword;
    }

    /**
     * @param s string to be parsed
     * @return corresponding role
     */
    public static Role fromString(String s) {
       if (s == null) {
           throw new InvalidInputException("Unknown role: null. Use regular or admin role.");
       }
       for (Role r : Role.values()) {
           if (r.getKeyword().equalsIgnoreCase(s)) {
               return r;
           }
       }
       throw new InvalidInputException("Unknown role: " + s + ". User regular or admin role.");
    }

}