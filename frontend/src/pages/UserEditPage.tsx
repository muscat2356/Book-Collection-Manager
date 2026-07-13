import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import type { User } from "../types/User";
import { fetchUserById, updateUser } from "../api/users";

export function UserEditPage(){
    type UserFormData = {
        displayName: string
        email: string
    }

    const navigate = useNavigate()
    const { id } = useParams()
    const [user, setUser] = useState<User | null>(null)
    const [formData, setFormData] = useState<UserFormData | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [submitting, setSubmitting] = useState(false)

    function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
        e.preventDefault()
        setError(null)
        setSubmitting(true)

        if(!id || !formData) return
        updateUser(id, formData)
        .then(() => navigate('/users'))
        .catch((error) => setError(error.message))
        .finally(() => setSubmitting(false))
    }

    useEffect(() => {
        if(!id){
            setError("IDが指定されていません")
            setLoading(false)
            return
        }

        fetchUserById(id)
            .then((data) => {
                if (!data) return
                setUser(data);
                setFormData({
                    displayName:data.displayName,
                    email: data.email
                });
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false))
    }, [id])

    if(loading) {
        return <p className="page-status">読み込み中・・・</p>
    }

    if(!user || !formData) {
        return <p className="page-status page-status--error">利用者が見つかりません</p>
    }

    return (
        <section className="page">
            <h1>対象利用者：{user.displayName}</h1>

            <form onSubmit={handleSubmit}>
                {error && (
                  <p className="page-status page-status--error">{error}</p>
                )}
                <div>
                    <label htmlFor="displayName">氏名：</label>
                    <input
                     id="displayName"
                     type="text"
                     value={formData.displayName}
                     onChange={(e) =>
                        setFormData({
                            ...formData,
                            displayName: e.target.value 
                        })
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
                        setFormData({
                            ...formData,
                            email: e.target.value 
                        })
                     }
                     required
                      />
                </div>
                <button type="submit" className="btn btn--primary" disabled={submitting}>
                {submitting ? '更新中...' : '更新'}
                </button>

            </form>

        </section>
    )
    
}
