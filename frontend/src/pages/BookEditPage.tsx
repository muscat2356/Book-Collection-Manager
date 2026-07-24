import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import type { Book, CopyStatus } from "../types/Book";
import { addBookCopy, deleteBook, deleteBookCopy, fetchBookById, updateBook } from "../api/books";
import { useApiClient } from "../api/ApiClientContext";
import type { CategoryNode } from "../types/Category";
import { fetchCategoryTree } from "../api/categories";

export function BookEditPage(){
    type BookFormData = {
        title: string
        author: string
        isbn: string
        publisher: string
    }

    const navigate = useNavigate()
    const { id } = useParams()
    const apiClient = useApiClient()
    const [book, setBook] = useState<Book | null>(null)
    const [formData, setFormData] = useState<BookFormData | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [copyError, setCopyError] = useState<string | null>(null)
    const [submitting, setSubmitting] = useState(false)
    const [deleting, setDeleting] = useState(false)
    const [copyBusy, setCopyBusy] = useState(false)
    const [tree, setTree] = useState<CategoryNode[]>([])
    const [largeId, setLargeId] = useState<number | "">("")
    const [mediumId, setMediumId] = useState<number | "">("")
    const [smallId, setSmallId] = useState<number | "">("")
    const [categoryLoading, setCategoryLoading] = useState(true)
    const [categoryError, setCategoryError] = useState<string | null>(null)

    
    function statusLabel(status: CopyStatus): string {
    return status === "AVAILABLE" ? "貸出可" : "貸出中"
    }

    function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
        e.preventDefault()
        if(!id || !formData) return
        setError(null)
        setSubmitting(true)

        updateBook(apiClient, id, { ...formData, categorySmallId: smallId === "" ? null : smallId,})
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
        if (latest) {
            setBook(latest)
          } else {
            setBook(null)
            setFormData(null)
            setError("書籍が見つかりません")
          }
      }

      async function handleAddCopy() {
        if (!id) return
        if (!window.confirm("所蔵を 1 冊追加しますか？")) return
        setCopyError(null)
        setCopyBusy(true)
        try {
          await addBookCopy(apiClient, id)
        } catch (err) {
          setCopyError(err instanceof Error ? err.message : "追加に失敗しました")
          setCopyBusy(false)
          return
        } 
        
        try {
            await reloadBook()
        } catch (err) {
            setCopyError(err instanceof Error ? err.message : "追加に成功しましたが、表示の更新に失敗しました")
        }
        finally {
          setCopyBusy(false)
        }
      }

      async function handleDeleteCopy(copyId: number) {
        if (!id) return
        if (!window.confirm(`所蔵 #${copyId} を削除しますか？`)) return
        setCopyError(null)
        setCopyBusy(true)
        try {
          await deleteBookCopy(apiClient, id, copyId)
        } catch (err) {
          setCopyError(err instanceof Error ? err.message : "削除に失敗しました")
          setCopyBusy(false)
          return
        }
        
        try {
            await reloadBook()
        } catch (err) {
            setCopyError(err instanceof Error ? err.message : "削除に成功しましたが、表示の更新に失敗しました")
        }
        finally {
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
                    publisher: data.publisher
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

    useEffect(() => {
        let ignore = false
        setCategoryLoading(true)
        setCategoryError(null)
        fetchCategoryTree(apiClient)
            .then((data) => {
                if(!ignore) setTree(data)
            })
            .catch((err) => {
            if(!ignore) {
                setCategoryError(err instanceof Error ? err.message : "カテゴリの取得に失敗しました")
            }
            })
            .finally(() => {
                if(!ignore) setCategoryLoading(false)
            })
        return () => {
            ignore = true
        }
    }, [apiClient])

    useEffect(() => {
        if (!book?.category || tree.length === 0) return
        setLargeId(book.category.largeId)
        setMediumId(book.category.mediumId)
        setSmallId(book.category.smallId)
      }, [book, tree])

    const largeNode = tree.find((n) => n.id === largeId)
    const mediumOptions = largeNode?.children ?? []
    const mediumNode = mediumOptions.find((n) => n.id === mediumId)
    const smallOptions = mediumNode?.children ?? []
    const categoryDisabled = categoryLoading || !!categoryError

    function handleLargeChange(value: string) {
        setLargeId(value === "" ? "" : Number(value))
        setMediumId("")
        setSmallId("")
      }
      function handleMediumChange(value: string) {
        setMediumId(value === "" ? "" : Number(value))
        setSmallId("")
      }


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

                <div>
                    <label htmlFor="publisher">出版社：</label>
                    <input
                     id="publisher"
                     type="text"
                     value={formData.publisher}
                     onChange={(e) =>
                        setFormData({...formData, publisher: e.target.value})
                     }
                     required />
                </div>

                {categoryError && (
                  <p className="page-status page-status--error">{categoryError}</p>
                )}

                <div className="category-cascade">
                  <p className="category-cascade__title">カテゴリ</p>
                  <div>
                    <label htmlFor="category-large">大カテゴリ：</label>
                    <select
                      id="category-large"
                      value={largeId === "" ? "" : String(largeId)}
                      onChange={(e) => handleLargeChange(e.target.value)}
                      disabled={categoryDisabled || submitting}
                    >
                      <option value="">（未選択）</option>
                      {tree.map((n) => (
                        <option key={n.id} value={n.id}>
                          {n.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label htmlFor="category-medium">中カテゴリ：</label>
                    <select
                      id="category-medium"
                      value={mediumId === "" ? "" : String(mediumId)}
                      onChange={(e) => handleMediumChange(e.target.value)}
                      disabled={categoryDisabled || submitting || largeId === ""}
                    >
                      <option value="">（未選択）</option>
                      {mediumOptions.map((n) => (
                        <option key={n.id} value={n.id}>
                          {n.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label htmlFor="category-small">小カテゴリ：</label>
                    <select
                      id="category-small"
                      value={smallId === "" ? "" : String(smallId)}
                      onChange={(e) =>
                        setSmallId(e.target.value === "" ? "" : Number(e.target.value))
                      }
                      disabled={categoryDisabled || submitting || mediumId === ""}
                    >
                      <option value="">（未分類）</option>
                      {smallOptions.map((n) => (
                        <option key={n.id} value={n.id}>
                          {n.name}
                        </option>
                      ))}
                    </select>
                  </div>
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

            {copyError && (
                <p className="page-status page-status--error">{copyError}</p>
            )}
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
