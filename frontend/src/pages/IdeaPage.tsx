import ComingSoonFeature from '../components/ComingSoonFeature'

export default function IdeaPage() {
  return (
    <ComingSoonFeature
      title="아이디어 제안"
      description="공모전 파일을 업로드하면 자료실의 회사 소개 자료를 자동으로 참고해 아이디어를 제안합니다."
      formHint="공모전 파일(docx/pdf/hwp) 업로드 — 회사 자료는 별도로 올릴 필요 없이 자료실에서 자동 조회됩니다."
    />
  )
}
