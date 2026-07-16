import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import type { Book, CopyStatus } from "../types/Book";
import { addBookCopy, deleteBook, deleteBookCopy, fetchBookById, updateBook } from "../api/books";
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
    const [copyBusy, setCopyBusy] = useState(false)

    function statusLabel(status: CopyStatus): string {
    return status === "AVAILABLE" ? "貸出可" : "貸出中"
    }

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

      async function reloadBook() {
        if (!id) return
        const latest = await fetchBookById(apiClient, id)
        if (latest) setBook(latest)
        else setError("書籍が見つかりません")
      }

      async function handleAddCopy() {
        if (!id) return
        if (!window.confirm("所蔵を 1 冊追加しますか？")) return
        setError(null)
        setCopyBusy(true)
        try {
          await addBookCopy(apiClient, id)
          await reloadBook()
        } catch (err) {
          setError(err instanceof Error ? err.message : "追加に失敗しました")
        } finally {
          setCopyBusy(false)
        }
      }

      async function handleDeleteCopy(copyId: number) {
        if (!id) return
        if (!window.confirm(`所蔵 #${copyId} を削除しますか？`)) return
        setError(null)
        setCopyBusy(true)
        try {
          await deleteBookCopy(apiClient, id, copyId)
          await reloadBook()
        } catch (err) {
          setError(err instanceof Error ? err.message : "削除に失敗しました")
        } finally {
          setCopyBusy(false)
        }
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
                 className="btn btn--danger"
                 onClick={handleDelete}
                 disabled={submitting || deleting}
                >
                    {deleting ? "削除中..." : "削除"}
                </button>

            </form>
            <section className="holdings">
            <h2 className="holdings__title">所蔵管理</h2>
            <p className="page__lead">
                貸出可 {book.availableCount} / 所蔵 {book.totalCount}
            </p>

            <button
                type="button"
                className="btn btn--primary"
                onClick={handleAddCopy}
                disabled={copyBusy || submitting || deleting}
            >
                {copyBusy ? "処理中…" : "所蔵を 1 冊追加"}
            </button>

            {(book.holdings ?? []).length === 0 ? (
                <p className="holdings__empty">所蔵がありません</p>
            ) : (
                <table className="holdings__table">
                <thead>
                    <tr>
                    <th scope="col">所蔵 ID</th>
                    <th scope="col">状態</th>
                    <th scope="col">操作</th>
                    </tr>
                </thead>
                <tbody>
                    {(book.holdings ?? []).map((h) => {
                    const canDelete = h.status === "AVAILABLE"
                    return (
                        <tr key={h.id}>
                        <td>#{h.id}</td>
                        <td>
                            <span
                            className={`holdings__status holdings__status--${h.status.toLowerCase()}`}
                            >
                            {statusLabel(h.status)}
                            </span>
                        </td>
                        <td>
                            <button
                            type="button"
                            className="btn btn--danger"
                            disabled={!canDelete || copyBusy || submitting || deleting}
                            onClick={() => handleDeleteCopy(h.id)}
                            >
                            削除
                            </button>
                        </td>
                        </tr>
                    )
                    })}
                </tbody>
                </table>
            )}
            </section>
        </section>
        
    )
    
}
