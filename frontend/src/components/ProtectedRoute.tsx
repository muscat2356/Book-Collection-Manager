import { useEffect } from "react"
import { useAuth } from "../auth/AuthContext"
import { isAdmin, isEmployee } from "../auth/roles"
import { Navigate, Outlet } from "react-router-dom"

type ProtectedRouteProps = {
    requireAdmin?: boolean
}

export function ProtectedRoute({ requireAdmin = false }: ProtectedRouteProps){
    const { keycloak, isAuthenticated, isInitialized } = useAuth()

    useEffect(() => {
        if(!isInitialized) return
        if(isAuthenticated) return
        keycloak?.login()
    }, [isAuthenticated, isInitialized, keycloak])

    if(!isInitialized) {
        return <p className="page-status">認証状態を確認中...</p>
    }

    if(!isAuthenticated){
        return <p className="page-status">ログイン画面へ移動中...</p>
    }

    if(!isEmployee(keycloak)) {
        return <Navigate to="/access-denied" replace />
    }

    if(requireAdmin && !isAdmin(keycloak)) {
        return <Navigate to="/access-denied" replace />
    }

    return <Outlet />
}
