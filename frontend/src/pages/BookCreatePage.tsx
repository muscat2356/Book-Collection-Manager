import React, { useState } from "react"
import { useNavigate } from "react-router-dom"
import { createBook } from "../api/books"
import { useApiClient } from "../api/ApiClientContext"

export function BookCreatePage(){
    type BookFormData = {
        title: string
        author: string
        isbn: string
        initialCopyCount: number    
    }

    const navigate = useNavigate()

    const [formData, setFormData] = useState<BookFormData>({
        title: '',
        author: '',
        isbn: '',
        initialCopyCount: 1
    })

    const apiClient = useApiClient()
    const[error, setError] = useState<string | null>(null)
    const[submitting, setSubmitting] = useState(false)

    function handleSubmit(e: React.SubmitEvent<HTMLFormElement>){
        e.preventDefault()
        setError(null)
        setSubmitting(true)

        createBook(apiClient, formData)
        .then(() => navigate('/books'))
        .catch((err) => setError(err.message))
        .finally(() => setSubmitting(false))
    }

    return (
        <section className="page">
            <h1>新規書籍登録</h1>
            <form onSubmit={handleSubmit}>
                {error && (
                    <p className="page-status page-status--error">{error}</p>
                )}
                <div>
                    <label htmlFor="title">タイトル：</label>
                    <input
                     id="title"
                     type="text"
                     value={formData.title}
                     onChange={(e) =>
                        setFormData({...formData, title: e.target.value})
                     }
                     required />
                </div>

                <div>
                    <label htmlFor="author">著者：</label>
                    <input
                     id="author"
                     type="text"
                     value={formData.author}
                     onChange={(e) =>
                        setFormData({...formData, author: e.target.value})
                     }
                     required />
                </div>

                <div>
                    <label htmlFor="isbn">ISBN：</label>
                    <input
                     id="isbn"
                     type="text"
                     value={formData.isbn}
                     onChange={(e) =>
                        setFormData({...formData, isbn: e.target.value})
                     }
                     required />
                </div>

                <div>
                    <label htmlFor="initialCopyCount">初期所蔵冊数：</label>
                    <input
                     id="initialCopyCount"
                     type="number"
                     value={formData.initialCopyCount}
                     onChange={(e) =>{
                        const n = e.target.valueAsNumber
                        setFormData({...formData, initialCopyCount: Number.isNaN(n) ? 1 : n,})
                    }}
                     min={1}
                     required />
                </div>

                <button type="submit" className="btn btn--primary" disabled={submitting}>
                    {submitting ? '登録中...' : "登録"}
                </button>
            </form>
        </section>
    )
}
