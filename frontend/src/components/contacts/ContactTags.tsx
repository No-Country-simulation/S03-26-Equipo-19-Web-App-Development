import React from 'react'
import { Badge } from '../ui/Badge'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from '../ui/dropdown-menu'
import { Plus, X } from 'lucide-react'
import { ScrollArea } from '../ui/scroll-area'
import type { ContactResType, Tag } from '../../types/contact.types'
import { useGetTags } from '../../services/use_queries/tags-query'

interface Props {
    contact: ContactResType
    onAddTag?: (tag: number) => void
    onRemoveTag?: (tag: number) => void
}

const ContactTags = ({ contact, onAddTag, onRemoveTag }: Props) => {


    const [isAddTagOpen, setIsAddTagOpen] = React.useState(false)

    const { data: tags } = useGetTags()

    const availableTagsFiltered = tags?.filter(
        (tag) => !contact.tags.some((t: Tag) => t.id === tag.id)
    )

    const handleAddTag = (tagId: number) => {
        onAddTag?.(tagId)
        setIsAddTagOpen(false)
    }

    const handleRemoveTag = (tagId: number) => {
        onRemoveTag?.(tagId)
    }

    return (
        <div className="space-y-2.5">
            <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-foreground">
                    Etiquetas
                </h3>
                <DropdownMenu open={isAddTagOpen} onOpenChange={setIsAddTagOpen}>
                    <DropdownMenuTrigger asChild >
                        <button
                            className="flex h-6 gap-1 text-xs text-primary hover:text-secondary transition-colors"
                        >
                            <Plus className="h-3 w-3" />
                            Etiqueta
                        </button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="start" className="w-48">
                        <DropdownMenuLabel>Estiquetas disponibles</DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        <ScrollArea className="h-48">
                            {availableTagsFiltered?.map((tag) => (
                                <DropdownMenuItem
                                    key={tag.id}
                                    onClick={() => handleAddTag(tag.id)}
                                >
                                    {tag.name}
                                </DropdownMenuItem>
                            ))}
                            {availableTagsFiltered?.length === 0 && (
                                <p className="px-2 py-4 text-center text-sm text-muted-foreground">
                                    Todas las etiquetas aplicadas
                                </p>
                            )}
                        </ScrollArea>
                    </DropdownMenuContent>
                </DropdownMenu>
            </div>
            <div className="flex flex-wrap gap-2.5 mt-3">
                {contact.tags.map((tag: Tag) => (
                    <Badge
                        key={tag.id}
                        className="bg-neutro-2/50 flex items-center gap-1"
                        onClick={() => handleRemoveTag(tag.id)}
                    >
                        {tag.name}
                        <X
                            className="h-3 w-3 cursor-pointer "
                        />
                    </Badge>
                ))}
            </div>
        </div>
    )
}

export default ContactTags
