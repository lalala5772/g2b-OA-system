import { useRef, useState, type DragEvent } from 'react'

interface FileUploaderProps {
  onFilesSelected: (files: File[]) => void
  accept?: string
  multiple?: boolean
  hint?: string
}

export default function FileUploader({
  onFilesSelected,
  accept,
  multiple = true,
  hint = 'docx · pdf · hwp',
}: FileUploaderProps) {
  const [isDragging, setIsDragging] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  function handleDrop(event: DragEvent<HTMLDivElement>) {
    event.preventDefault()
    setIsDragging(false)
    if (event.dataTransfer.files.length > 0) {
      onFilesSelected(Array.from(event.dataTransfer.files))
    }
  }

  return (
    <div
      onDragOver={(e) => {
        e.preventDefault()
        setIsDragging(true)
      }}
      onDragLeave={() => setIsDragging(false)}
      onDrop={handleDrop}
      onClick={() => inputRef.current?.click()}
      className={`flex cursor-pointer flex-col items-center justify-center rounded-lg border border-dashed px-6 py-14 text-center transition-colors ${
        isDragging ? 'border-accent bg-accent-soft' : 'border-hairline hover:border-muted'
      }`}
    >
      <p className="text-sm text-offwhite">파일을 드래그하거나 클릭하여 업로드</p>
      <p className="mt-2 text-xs text-muted">{hint}</p>
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        multiple={multiple}
        className="hidden"
        onChange={(e) => {
          if (e.target.files && e.target.files.length > 0) {
            onFilesSelected(Array.from(e.target.files))
          }
          e.target.value = ''
        }}
      />
    </div>
  )
}
