import React, { useState } from "react"
import { useNavigate } from "react-router-dom"
import { createUser } from "../api/users"
import { useApiClient } from "../api/ApiClientContext"

export function UserCreatePage(){
    type UserFormData = {
        displayName: string
        email: string
    }

    const navigate = useNavigate()

    const [formData, setFormdata] = useState<UserFormData>({
        displayName: '',
        email: '',
    })

    const apiClient = useApiClient()
    const[error, setError] = useState<string | null>(null)
    const[submitting, setSubmitting] = useState(false)

    function handleSubmit(e: React.SubmitEvent<HTMLFormElement>){
        e.preventDefault()
        setError(null)
        setSubmitting(true)

        createUser(apiClient, formData)
        .then(() => navigate('/users'))
        .catch((err) => setError(err.message))
        .finally(() => setSubmitting(false))
    }

    return(
        <section className="page">
        <h1>利用者登録</h1>

        <form onSubmit={handleSubmit}>
            {error && (
          <p className="page-status page-status--error">{error}</p>
        )}
            <div>
                <label htmlFor="daisplayName">氏名：</label>
                <input 
                id="displayName"
                type="text"
                value={formData.displayName}
                onChange={(e) =>
                    setFormdata({ ...formData, displayName: e.target.value })
                }
                required
                />
            </div>
            <div>
                <label htmlFor="email">メールアドレス：</label>
                <input 
                id="email"
                type="text"
                value={formData.email}
                onChange={(e) =>
                    setFormdata({ ...formData, email: e.target.value })
                }
                required
                />
            </div>
        <button type="submit" className="btn btn--primary" disabled={submitting}>
          {submitting ? '登録中...' : '登録'}
        </button>
        </form>
        </section>
    )
}
