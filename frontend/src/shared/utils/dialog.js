import { ElMessageBox } from 'element-plus'

/** ResponsiveDialog / el-dialog 테마 헤더 클래스 */
export const THEMED_DIALOG_CLASS = 'responsive-dialog--themed-header'

/** ElMessageBox 테마 헤더 클래스 */
export const THEMED_MESSAGE_BOX_CLASS = 'app-message-box--themed-header'

/**
 * 팝업(ResponsiveDialog)용 공통 props.
 * 헤더 테마색은 기본 적용되며, 호출부에서 제목·너비 등만 넘기면 된다.
 *
 * @example
 * <ResponsiveDialog v-bind="themedDialogProps({ title: '수정', width: 520 })" v-model="open">
 */
export function themedDialogProps(overrides = {}) {
  return {
    themedHeader: true,
    ...overrides,
  }
}

/**
 * 확인 팝업 (헤더에 테마색 적용).
 * ElMessageBox.confirm 대신 이 함수를 사용한다.
 */
export function confirmDialog(message, title = '확인', options = {}) {
  const { customClass, ...rest } = options
  return ElMessageBox.confirm(message, title, {
    type: 'warning',
    ...rest,
    customClass: [THEMED_MESSAGE_BOX_CLASS, customClass].filter(Boolean).join(' '),
  })
}

/**
 * 알림 팝업 (헤더에 테마색 적용).
 */
export function alertDialog(message, title = '알림', options = {}) {
  const { customClass, ...rest } = options
  return ElMessageBox.alert(message, title, {
    ...rest,
    customClass: [THEMED_MESSAGE_BOX_CLASS, customClass].filter(Boolean).join(' '),
  })
}
