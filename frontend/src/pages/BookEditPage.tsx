import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import type { Book } from "../types/Book";
import { deleteBook, fetchBookById, updateBook } from "../api/books";
import { useApiClient } from "../api/ApiClientContext";

export function BookEditPage(){
    type BookFormData = {
        title: string
        author: string
        isbn: string
    }

    const navigate = useNavigate()
    const { id } = useParams()
    const apiClient = useApiClient()
    const [book, setBook] = useState<Book | null>(null)
    const [formData, setFormData] = useState<BookFormData | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [submitting, setSubmitting] = useState(false)
    const [deleting, setDeleting] = useState(false)

    function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
        e.preventDefault()
        if(!id || !formData) return
        setError(null)
        setSubmitting(true)

        updateBook(apiClient, id, formData)
        .then(() => navigate(`/books/${id}`))
        .catch((error) => setError(error.message))
        .finally(() => setSubmitting(false))
    }

    function handleDelete() {
        if (!id) return
        if (!window.confirm("この書籍を削除しますか？")) return
        setError(null)
        setDeleting(true)
        deleteBook(apiClient, id)
          .then(() => navigate("/books"))
          .catch((err) => setError(err.message))
          .finally(() => setDeleting(false))
      }

    useEffect(() => {
        if(!id){
            setError("IDが指定されていません")
            setLoading(false)
            return
        }

        let ignore = false

        setError(null)
        setLoading(true)

        fetchBookById(apiClient, id)
            .then((data) => {
                if(ignore) return
                if (!data) {
                    setBook(null)
                    setFormData(null)
                    setError("書籍が見つかりません")
                    return
                }
                setBook(data);
                setFormData({
                    title: data.title,
                    author: data.author,
                    isbn: data.isbn,
                });
            })
            .catch((err) => { 
                if(ignore) return
                setFormData(null)
                setBook(null)
                setError(err.message)
            })
            .finally(() => {if(!ignore) setLoading(false)})

        return () => {
            ignore = true
        }
        
    }, [id, apiClient])

    if(loading) {
        return <p className="page-status">読み込み中・・・</p>
    }

    if(error && !book) {
        return <p className="page-status page-status--error">{error}</p>
    }

    if(!book || !formData) {
        return <p className="page-status page-status--error">書籍が見つかりません</p>
    }

    return (
        <section className="page">
            <h1>対象：{book.title}</h1>

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

                <button
                type="submit"
                className="btn btn--primary"
                disabled={submitting || deleting}
                >
                {submitting ? "更新中..." : "更新"}
                </button>

                <button
                 type="button"
                 className="btn btn--secondary"
                 onClick={handleDelete}
                 disabled={submitting || deleting}
                >
                    {deleting ? "削除中..." : "削除"}
                </button>

            </form>

        </section>
    )
    
}
