import React, { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { createBook } from "../api/books"
import { useApiClient } from "../api/ApiClientContext"
import type { CategoryNode } from "../types/Category"
import { fetchCategoryTree } from "../api/categories"

export function BookCreatePage(){
    type BookFormData = {
        title: string
        author: string
        isbn: string
        publisher: string
        initialCopyCount: number    
    }

    const navigate = useNavigate()

    const [formData, setFormData] = useState<BookFormData>({
        title: '',
        author: '',
        isbn: '',
        publisher:'',
        initialCopyCount: 1
    })
    const [tree, setTree] = useState<CategoryNode[]>([])
    const [largeId, setLargeId] = useState<number | "">("")
    const [mediumId, setMediumId] = useState<number | "">("")
    const [smallId, setSmallId] = useState<number | "">("")
    const [categoryLoading, setCategoryLoading] = useState(true)
    const [categoryError, setCategoryError] = useState<string | null>(null)

    const apiClient = useApiClient()
    const[error, setError] = useState<string | null>(null)
    const[submitting, setSubmitting] = useState(false)

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
                if (!ignore) setCategoryLoading(false)
            })
        return () => {
            ignore = true
        }
    }, [apiClient])

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

    function handleSubmit(e: React.SubmitEvent<HTMLFormElement>){
        e.preventDefault()
        setError(null)
        setSubmitting(true)

        createBook(apiClient, {...formData, categorySmallId: smallId === "" ? null : smallId,})
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
