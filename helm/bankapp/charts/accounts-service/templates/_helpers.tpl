{{- define "accounts.fullname" -}}
{{- printf "%s-accounts" .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "accounts.configName" -}}
{{- printf "%s-accounts-config" .Release.Name -}}
{{- end -}}

{{- define "accounts.secretName" -}}
{{- printf "%s-accounts-secret" .Release.Name -}}
{{- end -}}
