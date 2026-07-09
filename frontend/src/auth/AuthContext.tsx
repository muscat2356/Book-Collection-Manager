import Keycloak from "keycloak-js"
import { createContext, useContext, useEffect, useState, type ReactNode } from "react"

interface KeycloakContextType {
    keycloak: Keycloak | null
    isAuthenticated: boolean
    isInitialized: boolean
    getAccessToken: (minValiditySeconds?: number) => Promise<string | null>
}

const KeycloakContext = createContext<KeycloakContextType>({
    keycloak: null,
    isAuthenticated: false,
    isInitialized: false,
    getAccessToken: async () => null,
});

const keycloakInstance = new Keycloak({
    url: import.meta.env.VITE_KEYCLOAK_URL,
    realm: import.meta.env.VITE_KEYCLOAK_REALM,
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
  })

let initPromise: Promise<boolean> | null;

function initKeycloak (): Promise<boolean> {
    if(!initPromise){
        initPromise = keycloakInstance.init({
            onLoad: 'check-sso',
            pkceMethod: 'S256'
        })
    }
    return initPromise;
}

export const KeycloakProvider = ({children}: {children: ReactNode}) => { 
const [isAuthenticated, setIsAuthenticated] = useState(false);
const [isInitialized, setIsInitialized] = useState(false);

useEffect(() => {
    initKeycloak()
    .then((authenticated) => {
        setIsAuthenticated(authenticated);
        setIsInitialized(true);
    })
    .catch((err) => {
        console.error('Keycloak init faild', err)
        setIsInitialized(true)
    })
}, [])

async function getAccessToken(minValiditySeconds: number = 30): Promise<string | null>{
    if(!isInitialized) return null;
    if(!keycloakInstance.authenticated) return null

    try {
        await keycloakInstance.updateToken(minValiditySeconds)
        return keycloakInstance.token ?? null
    } catch {
        return null
    }
}

return(
    <KeycloakContext.Provider
     value={{keycloak: keycloakInstance, isAuthenticated, isInitialized, getAccessToken }}>
        {children}
     </KeycloakContext.Provider>
);
};

export const useAuth = () => useContext(KeycloakContext);
