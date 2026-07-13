import React, { useState } from "react"
import { useNavigate } from "react-router-dom"
import { createBook } from "../api/books"
import { useApiClient } from "../api/ApiClientContext"

export function BookCreatePage(){
    type BookFormData = {
        title: string
        author: string
        isbn: string
        stockCount: number    
    }

    const navigate = useNavigate()

    const [formData, setFormData] = useState<BookFormData>({
        title: '',
        author: '',
        isbn: '',
        stockCount: 0
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
                    <label htmlFor="stockCount">在庫：</label>
                    <input
                     id="stockCount"
                     type="number"
                     value={formData.stockCount}
                     onChange={(e) =>
                        setFormData({...formData, stockCount: e.target.valueAsNumber})
                     }
                     required />
                </div>

                <button type="submit" className="btn btn--primary" disabled={submitting}>
                    {submitting ? '登録中...' : "登録"}
                </button>
            </form>
        </section>
    )
}
