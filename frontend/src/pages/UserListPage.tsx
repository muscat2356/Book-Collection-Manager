import { useEffect, useState } from "react"
import { fetchUsers } from "../api/users"
import type { User } from "../types/User"
import { UserCard } from "../components/UserCard"
import { Link } from "react-router-dom"
import { useApiClient } from "../api/ApiClientContext"

export function UserListPage(){
    const apiClient = useApiClient()
    const [users, setUsers] = useState<User[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        fetchUsers(apiClient)
        .then((data) => setUsers(data))
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false))
    }, [apiClient])

    if(loading){
        return <p className="page-status">読み込み中・・・</p>
    }

    if(error) {
        return <p className="page-status page-status--error">{error}</p>
    }

    return(
        <section className="page">
            <h1>利用者一覧</h1>
            <p className="page__lead">登録利用者の一覧です</p>
            <Link to="/users/new" className="book-card__link">利用者新規登録</Link>
            <section className="book-list">
                {users.map((user) => (
                    <UserCard key={user.id} user={user}/>
                ))}
            </section>
        </section>
    )
}
