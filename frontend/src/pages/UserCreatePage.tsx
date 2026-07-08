import React, { useState } from "react"
import { useNavigate } from "react-router-dom"
import { createUser } from "../api/users"

export function UserCreatePage(){
    type UserFormDate = {
        displayName: string
        email: string
        temporaryPassword: string
    }

    const navigate = useNavigate()

    const [formData, setFormdata] = useState<UserFormDate>({
        displayName: '',
        email: '',
        temporaryPassword: ''
    })

    const[error, setError] = useState<string | null>(null)
    const[submitting, setSubmitting] = useState(false)

    function handleSubmit(e: React.SubmitEvent<HTMLFormElement>){
        e.preventDefault()
        setError(null)
        setSubmitting(true)

        createUser(formData)
        .then(() => navigate('/users'))
        .catch((err) => setError(err.message))
        .finally(() => setSubmitting(false))
    }

    return(
        <section className="page">
        <h1>利用者登録</h1>

        <form onSubmit={handleSubmit}>
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

            <div>
                <label htmlFor="temporaryPassword">パスワード：</label>
                <input 
                id="temporaryPassword"
                type="password"
                value={formData.temporaryPassword}
                onChange={(e) =>
                    setFormdata({ ...formData, temporaryPassword: e.target.value })
                }
                required
                />
            </div>
            {error && (
          <p className="page-status page-status--error">{error}</p>
        )}
        <button type="submit" className="btn btn--primary" disabled={submitting}>
          {submitting ? '登録中...' : '登録'}
        </button>
        </form>
        </section>
    )
}
