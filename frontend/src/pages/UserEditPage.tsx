import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import type { User } from "../types/User";
import { deleteUser, fetchUserById, updateUser } from "../api/users";
import { useApiClient } from "../api/ApiClientContext";

export function UserEditPage(){
    type UserFormData = {
        displayName: string
        email: string
    }

    const navigate = useNavigate()
    const apiClient = useApiClient()
    const { id } = useParams()
    const [user, setUser] = useState<User | null>(null)
    const [formData, setFormData] = useState<UserFormData | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [submitting, setSubmitting] = useState(false)
    const [deleting, setDeleting] = useState(false)

    function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
        if(!id || !formData) return
        e.preventDefault()
        setError(null)
        setSubmitting(true)

        updateUser(apiClient, id, formData)
        .then(() => navigate('/users'))
        .catch((error) => setError(error.message))
        .finally(() => setSubmitting(false))
    }

    function handleDelete() {
        if(!id) return
        if(!window.confirm("この利用者を削除しますか？"))return
        setError(null)
        setDeleting(true)
        deleteUser(apiClient, id)
        .then(() => navigate("/users"))
        .catch((err) => setError(err.message))
        .finally(() => setDeleting(false))
    }

    useEffect(() => {
        if(!id){
            setError("IDが指定されていません")
            setLoading(false)
            return
        }

        fetchUserById(apiClient, id)
            .then((data) => {
                if (!data) {
                    setError("利用者が見つかりません")
                    return
                }
                setUser(data);
                setFormData({
                    displayName:data.displayName,
                    email: data.email
                });
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false))
    }, [id, apiClient])

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
                    <label htmlFor="daisplayName">氏名：</label>
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
                {submitting || deleting ? '更新中...' : '更新'}
                </button>

                <button
                 type="button"
                 className="btn btn--primary"
                 disabled={deleting}
                 onClick={handleDelete} >
                    {submitting || deleting ? '削除中...' : '削除'}
                </button>
            </form>

        </section>
    )
    
}
