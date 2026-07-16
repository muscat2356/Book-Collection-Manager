import { useNavigate } from "react-router-dom";
import { useApiClient } from "../api/ApiClientContext";
import { useLoanCheckout } from "../loans/LoanContext";
import { useEffect, useState } from "react";
import type { User } from "../types/User";
import { fetchUsers } from "../api/users";

export function CheckoutUserSelectPage() {
    const apiClient = useApiClient()
    const navigate = useNavigate()
    const { setUser } = useLoanCheckout()

    const [users, setUsers] = useState<User[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        let ignore = false;
        (async () => {
        try {
            setLoading(true)
            const data = await fetchUsers(apiClient)
            if(!ignore) {
                setUsers(data.filter((u) => u.isActive))
            }
        } catch (err) {
            if(!ignore){
                setError(err instanceof Error ? err.message : "取得に失敗しました")
            }
        }
        finally{
            if(!ignore) setLoading(false)
        }
    })()
    return () => {
        ignore = true
    }
    }, [apiClient])

    function handleSelect(u: User) {
        setUser({id: u.id, displayName: u.displayName })
        navigate("/loans/checkout/books")
    }

    if (loading) return <p className="page-status">読み込み中…</p>
    if (error) return <p className="page-status page-status--error">{error}</p>

    return (
        <section>
          <h1>貸出 — 利用者を選ぶ</h1>
          <p className="page__lead">貸出先の利用者を選んでください</p>
          <ul className="book-list">
            {users.map((u) => (
              <li key={u.id}>
                <button
                  type="button"
                  className="book-card"
                  onClick={() => handleSelect(u)}
                >
                  <strong>{u.displayName}</strong>
                  <div>{u.email}</div>
                </button>
              </li>
            ))}
          </ul>
        </section>
      )
    }
