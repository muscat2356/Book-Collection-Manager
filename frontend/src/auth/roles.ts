import type Keycloak from "keycloak-js";

export type AppRoles = 'general_employee' | 'admin_employee'

export function getRoles(keycloak: Keycloak | null): string[] {
    return keycloak?.tokenParsed?.realm_access?.roles
}

export function isEmployee(keycloak: Keycloak | null): boolean {
    const roles = getRoles(keycloak)
    return roles.includes('general_employee') || roles.includes('admin_employee')
}

export function isAdmin(keycloak: Keycloak | null): boolean {
    return getRoles(keycloak).includes('admin_employee')
}
