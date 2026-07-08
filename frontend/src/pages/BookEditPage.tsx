import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import type { Book } from "../types/Book";
import { fetchBookById, updateBook } from "../api/books";

export function BookEditPage(){
    type BookFormData = {
        title: string
        author: string
        isbn: string
        stockCount: number    
    }

    const navigate = useNavigate()
    const { id } = useParams()
    const [book, setBook] = useState<Book | null>(null)
    const [formData, setFormData] = useState<BookFormData | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [submitting, setSubmitting] = useState(false)

    function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
        e.preventDefault()
        setError(null)
        setSubmitting(true)

        if(!id || !formData) return
        updateBook(id, formData)
        .then(() => navigate(`/books/${id}`))
        .catch((error) => setError(error.message))
        .finally(() => setSubmitting(false))
    }

    useEffect(() => {
        if(!id){
            setError("IDが指定されていません")
            setLoading(false)
            return
        }

        fetchBookById(id)
            .then((data) => {
                if (!data) return
                setBook(data);
                setFormData({
                    title: data.title,
                    author: data.author,
                    isbn: data.isbn,
                    stockCount: data.stockCount    
                });
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false))
    }, [id])

    if(loading) {
        return <p className="page-status">読み込み中・・・</p>
    }

    if(!book || !formData) {
        return <p className="page-status page-status--error">利用者が見つかりません</p>
    }

    return (
        <section className="page">
            <h1>対象：{book.title}</h1>

            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="titie">タイトル：</label>
                    <input
                     id="title"
                     type="text"
                     value={formData.title}
                     onChange={(e) =>
                        setFormData({
                            ...formData,
                            title: e.target.value 
                        })
                     }
                     required
                      />
                </div>

                <div>
                    <label htmlFor="author">著者：</label>
                    <input
                     id="author"
                     type="text"
                     value={formData.author}
                     onChange={(e) =>
                        setFormData({
                            ...formData,
                            author: e.target.value 
                        })
                     }
                     required
                      />
                </div>

                <div>
                    <label htmlFor="isbn">isbn：</label>
                    <input
                     id="isbn"
                     type="text"
                     value={formData.isbn}
                     onChange={(e) =>
                        setFormData({
                            ...formData,
                            isbn: e.target.value 
                        })
                     }
                     required
                      />
                </div>

                <div>
                    <label htmlFor="stockCount">在庫：</label>
                    <input
                     id="stockCount"
                     type="number"
                     value={formData.stockCount}
                     onChange={(e) =>
                        setFormData({
                            ...formData,
                            stockCount: e.target.valueAsNumber 
                        })
                     }
                     required
                      />
                </div>

                {error && (
                  <p className="page-status page-status--error">{error}</p>
                )}
                <button type="submit" className="btn btn--primary" disabled={submitting}>
                {submitting ? '更新中...' : '更新'}
                </button>

            </form>

        </section>
    )
    
}
